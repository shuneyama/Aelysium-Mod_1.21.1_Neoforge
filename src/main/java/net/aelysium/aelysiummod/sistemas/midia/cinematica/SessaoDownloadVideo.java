package net.aelysium.aelysiummod.sistemas.midia.cinematica;

import net.aelysium.aelysiummod.sistemas.midia.cinematica.ConfiguracaoTransferenciaVideo;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteChunkArquivo;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteFinalizarDownload;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteIniciarDownload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SessaoDownloadVideo {

    private static final Map<UUID, Map<String, SessaoDownloadVideo>> sessoes = new ConcurrentHashMap<>();

    private static String chave(String nome, String tipo) {
        return nome + ":" + tipo;
    }

    public static SessaoDownloadVideo obter(UUID jogador, String nome, String tipo) {
        Map<String, SessaoDownloadVideo> m = sessoes.get(jogador);
        return m == null ? null : m.get(chave(nome, tipo));
    }

    public static void remover(UUID jogador, String nome, String tipo) {
        Map<String, SessaoDownloadVideo> m = sessoes.get(jogador);
        if (m != null) m.remove(chave(nome, tipo));
    }

    public static SessaoDownloadVideo criar(ServerPlayer player, String nome, String tipo) {
        SessaoDownloadVideo s = new SessaoDownloadVideo(player, nome, tipo);
        sessoes.computeIfAbsent(player.getUUID(), k -> new HashMap<>()).put(chave(nome, tipo), s);
        return s;
    }

    public enum Estado { CRIADA, ENVIANDO, AGUARDANDO_CONFIRMACAO, CONCLUIDA, FALHOU }

    private final ServerPlayer player;
    private final String nome;
    private final String tipo;
    private final long inicioMillis;
    private Estado estado;
    private String mensagemFalha;
    private long totalBytesEnviados;
    private Consumer<SessaoDownloadVideo> aoConcluir;
    private Consumer<SessaoDownloadVideo> aoProgresso;
    private boolean jaTinhaCache;
    private String sha;

    private SessaoDownloadVideo(ServerPlayer player, String nome, String tipo) {
        this.player = player;
        this.nome = nome;
        this.tipo = tipo;
        this.inicioMillis = System.currentTimeMillis();
        this.estado = Estado.CRIADA;
    }

    public String nome() { return nome; }
    public String tipo() { return tipo; }
    public Estado estado() { return estado; }
    public String mensagemFalha() { return mensagemFalha; }
    public ServerPlayer jogador() { return player; }
    public long totalBytesEnviados() { return totalBytesEnviados; }
    public long duracaoMillis() { return System.currentTimeMillis() - inicioMillis; }
    public boolean jaTinhaCache() { return jaTinhaCache; }
    public String sha() { return sha; }

    public void aoConcluir(Consumer<SessaoDownloadVideo> callback) {
        this.aoConcluir = callback;
    }

    public void aoProgresso(Consumer<SessaoDownloadVideo> callback) {
        this.aoProgresso = callback;
    }

    public void marcarConcluido(boolean jaTinha) {
        jaTinhaCache = jaTinha;
        estado = Estado.CONCLUIDA;
        if (aoConcluir != null) aoConcluir.accept(this);
        remover(player.getUUID(), nome, tipo);
    }

    public void marcarFalha(String msg) {
        estado = Estado.FALHOU;
        mensagemFalha = msg;
        if (aoConcluir != null) aoConcluir.accept(this);
        remover(player.getUUID(), nome, tipo);
    }

    public void enviarArquivo(Path arquivo, String tipoArquivo, String sha256) {
        this.sha = sha256;
        Thread t = new Thread(() -> enviarSync(arquivo, tipoArquivo, sha256),
                "AelysiumVideoSender-" + player.getName().getString() + "-" + nome + "-" + tipoArquivo);
        t.setDaemon(true);
        t.start();
    }

    private void enviarSync(Path arquivo, String tipoArquivo, String sha256) {
        estado = Estado.ENVIANDO;
        try {
            long total = java.nio.file.Files.size(arquivo);
            int chunkSize = ConfiguracaoTransferenciaVideo.CHUNK_SIZE;
            int totalChunks = (int) ((total + chunkSize - 1) / chunkSize);

            PacketDistributor.sendToPlayer(player,
                    new PacoteIniciarDownload(nome, tipoArquivo, total, totalChunks, sha256));

            long restante;
            try (RandomAccessFile raf = new RandomAccessFile(arquivo.toFile(), "r")) {
                byte[] buffer = new byte[chunkSize];
                int indice = 0;
                restante = total;
                while (restante > 0 && player.connection != null && !player.hasDisconnected()
                        && estado != Estado.CONCLUIDA && estado != Estado.FALHOU) {
                    int aLer = (int) Math.min(chunkSize, restante);
                    raf.readFully(buffer, 0, aLer);
                    byte[] dados = aLer == chunkSize ? buffer.clone() : java.util.Arrays.copyOf(buffer, aLer);
                    PacketDistributor.sendToPlayer(player, new PacoteChunkArquivo(nome, tipoArquivo, indice, dados));
                    indice++;
                    totalBytesEnviados += aLer;
                    restante -= aLer;
                    if (aoProgresso != null) aoProgresso.accept(this);
                    Thread.sleep(ConfiguracaoTransferenciaVideo.INTERVALO_CHUNK_MS);
                }
            }

            if (estado == Estado.ENVIANDO) {
                if (restante > 0) {
                    marcarFalha("o jogador desconectou durante o envio");
                    return;
                }
                PacketDistributor.sendToPlayer(player, new PacoteFinalizarDownload(nome, tipoArquivo));
                estado = Estado.AGUARDANDO_CONFIRMACAO;
            }
        } catch (IOException | InterruptedException e) {
            marcarFalha(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
