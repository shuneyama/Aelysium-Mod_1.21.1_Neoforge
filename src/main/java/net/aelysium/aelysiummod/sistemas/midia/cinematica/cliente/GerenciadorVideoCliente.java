package net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente;

import net.aelysium.aelysiummod.sistemas.midia.cinematica.ConfiguracaoTransferenciaVideo;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteStatusDownload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GerenciadorVideoCliente {

    public static final String SUBPASTA = "aelysium_cache/videos";

    private static final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();
    private static volatile String hashServidor = "default";

    public static void definirHashServidor(String hash) {
        hashServidor = hash == null || hash.isBlank() ? "default" : hash;
    }

    public static Path pastaCache() {
        Path base = Minecraft.getInstance().gameDirectory.toPath().resolve(SUBPASTA).resolve(hashServidor);
        try {
            if (!Files.exists(base)) Files.createDirectories(base);
        } catch (IOException ignored) {}
        return base;
    }

    public static Path arquivoCache(String nome, String tipo) {
        return pastaCache().resolve(nome + "." + tipo);
    }

    public static Path arquivoHash(String nome, String tipo) {
        return pastaCache().resolve(nome + "." + tipo + ".sha256");
    }

    public static boolean temArquivoComHash(String nome, String tipo, String shaEsperado) {
        Path arq = arquivoCache(nome, tipo);
        Path sha = arquivoHash(nome, tipo);
        if (!Files.isRegularFile(arq) || !Files.isRegularFile(sha)) return false;
        try {
            String shaSalvo = Files.readString(sha).trim();
            return shaSalvo.equalsIgnoreCase(shaEsperado);
        } catch (IOException e) {
            return false;
        }
    }

    private static class Sessao {
        final String nome;
        final String tipo;
        final long totalBytes;
        final int totalChunks;
        final String shaEsperado;
        final Path arquivoParcial;
        final Path arquivoFinal;
        RandomAccessFile raf;
        int chunksRecebidos;

        Sessao(String nome, String tipo, long totalBytes, int totalChunks, String shaEsperado) {
            this.nome = nome;
            this.tipo = tipo;
            this.totalBytes = totalBytes;
            this.totalChunks = totalChunks;
            this.shaEsperado = shaEsperado;
            this.arquivoFinal = arquivoCache(nome, tipo);
            this.arquivoParcial = pastaCache().resolve(nome + "." + tipo + ".part");
        }
    }

    public static void iniciarRecepcao(String nome, String tipo, long totalBytes, int totalChunks, String sha) {
        String chave = nome + ":" + tipo;
        if (temArquivoComHash(nome, tipo, sha)) {
            PacketDistributor.sendToServer(new PacoteStatusDownload(nome, tipo, PacoteStatusDownload.STATUS_JA_TEM, "cache válido"));
            return;
        }
        try {
            Sessao s = new Sessao(nome, tipo, totalBytes, totalChunks, sha);
            Files.deleteIfExists(s.arquivoParcial);
            s.raf = new RandomAccessFile(s.arquivoParcial.toFile(), "rw");
            s.raf.setLength(totalBytes);
            sessoes.put(chave, s);
        } catch (IOException e) {
            PacketDistributor.sendToServer(new PacoteStatusDownload(nome, tipo, PacoteStatusDownload.STATUS_FALHA_IO, "io: " + e.getMessage()));
        }
    }

    public static void receberChunk(String nome, String tipo, int indice, byte[] dados) {
        String chave = nome + ":" + tipo;
        Sessao s = sessoes.get(chave);
        if (s == null) return;
        try {
            long offset = (long) indice * (long) ConfiguracaoTransferenciaVideo.CHUNK_SIZE;
            s.raf.seek(offset);
            s.raf.write(dados);
            s.chunksRecebidos++;
        } catch (IOException e) {
            try { s.raf.close(); } catch (IOException ignored) {}
            sessoes.remove(chave);
            PacketDistributor.sendToServer(new PacoteStatusDownload(nome, tipo, PacoteStatusDownload.STATUS_FALHA_IO, "chunk: " + e.getMessage()));
        }
    }

    public static void finalizarRecepcao(String nome, String tipo) {
        String chave = nome + ":" + tipo;
        Sessao s = sessoes.remove(chave);
        if (s == null) return;
        try {
            s.raf.close();
            String shaCalculado = calcularSha256(s.arquivoParcial);
            if (!shaCalculado.equalsIgnoreCase(s.shaEsperado)) {
                Files.deleteIfExists(s.arquivoParcial);
                PacketDistributor.sendToServer(new PacoteStatusDownload(nome, tipo, PacoteStatusDownload.STATUS_FALHA_HASH,
                        "esperado=" + s.shaEsperado.substring(0, Math.min(8, s.shaEsperado.length()))
                                + " obtido=" + shaCalculado.substring(0, Math.min(8, shaCalculado.length()))));
                return;
            }
            Files.move(s.arquivoParcial, s.arquivoFinal, StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(arquivoHash(nome, tipo), shaCalculado);
            PacketDistributor.sendToServer(new PacoteStatusDownload(nome, tipo, PacoteStatusDownload.STATUS_SUCESSO, "ok"));
        } catch (IOException e) {
            PacketDistributor.sendToServer(new PacoteStatusDownload(nome, tipo, PacoteStatusDownload.STATUS_FALHA_IO, "final: " + e.getMessage()));
        }
    }

    public static String calcularSha256(Path arquivo) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (java.io.InputStream in = Files.newInputStream(arquivo)) {
                byte[] buf = new byte[64 * 1024];
                int n;
                while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
            }
            return HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static boolean videoDisponivel(String nome) {
        return Files.isRegularFile(arquivoCache(nome, "mp4"));
    }

    public static boolean audioDisponivel(String nome) {
        return Files.isRegularFile(arquivoCache(nome, "ogg"));
    }
}
