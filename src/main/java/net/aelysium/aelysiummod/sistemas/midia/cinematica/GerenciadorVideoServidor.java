package net.aelysium.aelysiummod.sistemas.midia.cinematica;

import net.aelysium.aelysiummod.sistemas.midia.cinematica.ConfiguracaoTransferenciaVideo;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteStatusDownload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class GerenciadorVideoServidor {

    public static final String NOME_PASTA = "aelysium/videos";
    public static final long TAMANHO_MAXIMO_BYTES = ConfiguracaoTransferenciaVideo.TAMANHO_MAXIMO_BYTES;
    public static final int CHUNK_SIZE = ConfiguracaoTransferenciaVideo.CHUNK_SIZE;

    private static final Map<UUID, Map<String, Integer>> ultimoStatus = new ConcurrentHashMap<>();
    private static final Map<String, String> cacheHash = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, String>> confirmados = new ConcurrentHashMap<>();

    public static Path pastaVideos(MinecraftServer server) {
        Path raiz = server.getWorldPath(LevelResource.ROOT);
        Path pasta = raiz.resolve(NOME_PASTA);
        net.aelysium.aelysiummod.nucleo.armazenamento.MigracaoPasta.moverPasta(raiz.resolve("aelysium_videos"), pasta);
        try {
            if (!Files.exists(pasta)) Files.createDirectories(pasta);
        } catch (IOException ignored) {}
        return pasta;
    }

    public static boolean nomeValido(String nome) {
        if (nome == null || nome.isBlank()) return false;
        if (nome.contains("/") || nome.contains("\\") || nome.contains("..")) return false;
        if (nome.length() > 64) return false;
        for (int i = 0; i < nome.length(); i++) {
            char c = nome.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9') || c == '_' || c == '-';
            if (!ok) return false;
        }
        return true;
    }

    public static Path arquivoVideo(MinecraftServer server, String nome) {
        return pastaVideos(server).resolve(nome + ".mp4");
    }

    public static Path arquivoAudio(MinecraftServer server, String nome) {
        return pastaVideos(server).resolve(nome + ".ogg");
    }

    public static boolean existeVideo(MinecraftServer server, String nome) {
        return Files.isRegularFile(arquivoVideo(server, nome));
    }

    public static boolean existeAudio(MinecraftServer server, String nome) {
        return Files.isRegularFile(arquivoAudio(server, nome));
    }

    public static List<String> listarVideos(MinecraftServer server) {
        List<String> nomes = new ArrayList<>();
        Path pasta = pastaVideos(server);
        if (!Files.isDirectory(pasta)) return nomes;
        try (Stream<Path> stream = Files.list(pasta)) {
            stream.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(n -> n.toLowerCase().endsWith(".mp4"))
                    .map(n -> n.substring(0, n.length() - 4))
                    .filter(GerenciadorVideoServidor::nomeValido)
                    .sorted()
                    .forEach(nomes::add);
        } catch (IOException ignored) {}
        return nomes;
    }

    public static String calcularSha256(Path arquivo) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(arquivo);
            return HexFormat.of().formatHex(md.digest(bytes));
        } catch (Exception e) {
            return "";
        }
    }

    public static String hashCache(Path arquivo) {
        try {
            String key = arquivo.toAbsolutePath().toString() + ":" + Files.getLastModifiedTime(arquivo).toMillis() + ":" + Files.size(arquivo);
            String cached = cacheHash.get(key);
            if (cached != null) return cached;
            String calc = calcularSha256(arquivo);
            cacheHash.put(key, calc);
            return calc;
        } catch (Exception e) {
            return calcularSha256(arquivo);
        }
    }

    public static void registrarStatus(ServerPlayer player, String nome, String tipo, int status, String mensagem) {
        ultimoStatus.computeIfAbsent(player.getUUID(), k -> new HashMap<>()).put(nome, status);
        SessaoDownloadVideo sessao = SessaoDownloadVideo.obter(player.getUUID(), nome, tipo);
        if (sessao != null) {
            if (status == PacoteStatusDownload.STATUS_SUCESSO || status == PacoteStatusDownload.STATUS_JA_TEM) {
                if (sessao.tipo().equals("mp4") && sessao.sha() != null) {
                    confirmados.computeIfAbsent(player.getUUID(), k -> new ConcurrentHashMap<>()).put(nome, sessao.sha());
                }
                sessao.marcarConcluido(status == PacoteStatusDownload.STATUS_JA_TEM);
            } else {
                sessao.marcarFalha(descreverFalha(status, mensagem));
            }
        }
    }

    private static String descreverFalha(int status, String mensagem) {
        return switch (status) {
            case PacoteStatusDownload.STATUS_FALHA_HASH -> "arquivo corrompido na transferência (" + mensagem + ")";
            case PacoteStatusDownload.STATUS_FALHA_IO -> "erro de disco no cliente (" + mensagem + ")";
            case PacoteStatusDownload.STATUS_CANCELADO -> "cancelado pelo cliente";
            default -> mensagem;
        };
    }

    public static boolean estaConfirmado(UUID uuid, String nome, String shaAtual) {
        Map<String, String> m = confirmados.get(uuid);
        return m != null && shaAtual != null && shaAtual.equalsIgnoreCase(m.get(nome));
    }

    public static int statusJogador(UUID uuid, String nome) {
        Map<String, Integer> m = ultimoStatus.get(uuid);
        if (m == null) return -1;
        Integer v = m.get(nome);
        return v == null ? -1 : v;
    }

    public static void enviarMensagemAdmins(MinecraftServer server, Component msg, int permLevel) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.hasPermissions(permLevel)) {
                p.sendSystemMessage(msg);
            }
        }
    }

    public static String tamanhoLegivel(long bytes) {
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return String.format("%.1fKB", bytes / 1024.0);
        if (bytes < 1024L * 1024L * 1024L) return String.format("%.1fMB", bytes / (1024.0 * 1024.0));
        return String.format("%.2fGB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}
