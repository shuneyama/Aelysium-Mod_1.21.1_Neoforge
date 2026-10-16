package net.aelysium.aelysiummod.sistemas.midia.cinematica;

import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteStatusCutscene;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class RelatorioCutscene {

    public enum Modo { BAIXAR, TOCAR }

    private static final int TOTAL_FASES = 3;
    private static final Map<UUID, RelatorioCutscene> ATIVOS_TOCAR = new ConcurrentHashMap<>();
    private static final long TEMPO_LIMITE_RESUMO_SEGUNDOS = 300;
    private static final ScheduledExecutorService AGENDADOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "aelysium-relatorio-cutscene");
        t.setDaemon(true);
        return t;
    });

    private static class Alvo {
        final ServerPlayer jogador;
        long enviadoVideo;
        long enviadoAudio;
        int marcoEnvio = 0;
        int marcoPreparo = 0;
        long inicioMillis = System.currentTimeMillis();
        boolean videoOk;
        boolean audioOk;
        boolean jaTinha;
        boolean arquivosProntos;
        boolean terminado;
        boolean falhou;
        String falha = "";
        String faseDaFalha = "";

        Alvo(ServerPlayer jogador) {
            this.jogador = jogador;
        }
    }

    private final Modo modo;
    private final CommandSourceStack origem;
    private final MinecraftServer servidor;
    private final String nome;
    private final long bytesVideo;
    private final long bytesAudio;
    private final long inicioMillis = System.currentTimeMillis();
    private final Map<UUID, Alvo> alvos = new LinkedHashMap<>();
    private final boolean detalhado;
    private int faseAnunciada = 0;
    private int marcoMediaEnvio = 0;
    private int marcoMediaPreparo = 0;
    private boolean resumoEnviado = false;

    private RelatorioCutscene(Modo modo, CommandSourceStack origem, String nome, List<ServerPlayer> jogadores, long bytesVideo, long bytesAudio) {
        this.modo = modo;
        this.origem = origem;
        this.servidor = origem.getServer();
        this.nome = nome;
        this.bytesVideo = bytesVideo;
        this.bytesAudio = bytesAudio;
        for (ServerPlayer p : jogadores) alvos.put(p.getUUID(), new Alvo(p));
        this.detalhado = jogadores.size() <= 3;
    }

    public static RelatorioCutscene iniciarDownload(CommandSourceStack origem, String nome, List<ServerPlayer> jogadores, long bytesVideo, long bytesAudio) {
        RelatorioCutscene relatorio = new RelatorioCutscene(Modo.BAIXAR, origem, nome, jogadores, bytesVideo, bytesAudio);
        relatorio.anunciarInicioDownload();
        AGENDADOR.schedule(relatorio::encerrarPorTempo, TEMPO_LIMITE_RESUMO_SEGUNDOS, TimeUnit.SECONDS);
        return relatorio;
    }

    public static RelatorioCutscene iniciarTocar(CommandSourceStack origem, String nome, List<ServerPlayer> jogadores) {
        RelatorioCutscene relatorio = new RelatorioCutscene(Modo.TOCAR, origem, nome, jogadores, 0, 0);
        for (ServerPlayer p : jogadores) ATIVOS_TOCAR.put(p.getUUID(), relatorio);
        relatorio.anunciarFase(1, "Verificando se os " + jogadores.size() + " jogador(es) baixaram a cutscene §6" + nome + "§f...");
        AGENDADOR.schedule(relatorio::encerrarPorTempo, TEMPO_LIMITE_RESUMO_SEGUNDOS, TimeUnit.SECONDS);
        return relatorio;
    }

    public static void aoStatusDoCliente(ServerPlayer jogador, PacoteStatusCutscene pacote) {
        RelatorioCutscene relatorio = ATIVOS_TOCAR.get(jogador.getUUID());
        if (relatorio == null || !relatorio.nome.equals(pacote.nome())) return;
        relatorio.statusDoCliente(jogador, pacote);
    }

    private long bytesTotaisPorAlvo() {
        return bytesVideo + bytesAudio;
    }

    private void anunciarInicioDownload() {
        long taxa = ConfiguracaoTransferenciaVideo.CHUNK_SIZE * 1000L / Math.max(1L, ConfiguracaoTransferenciaVideo.INTERVALO_CHUNK_MS);
        long segundos = Math.max(1, (long) Math.ceil(bytesTotaisPorAlvo() / (double) taxa));
        String tamanho = GerenciadorVideoServidor.tamanhoLegivel(bytesVideo)
                + (bytesAudio > 0 ? " + áudio " + GerenciadorVideoServidor.tamanhoLegivel(bytesAudio) : "");
        anunciarFase(1, "Preparando o download da cutscene §6" + nome + "§f: vídeo " + tamanho + " para " + alvos.size()
                + " jogador(es). Envio estimado em até ~" + segundos + " s por jogador, em paralelo.");
    }

    public synchronized void avisoCalculandoVerificacao() {
        mensagemExecutor("§7    Calculando a verificação (SHA-256) dos arquivos, isso pode levar alguns segundos...");
    }

    public synchronized void enviandoArquivos() {
        anunciarFase(2, "Enviando os arquivos para os jogadores...");
    }

    public synchronized void envioProgresso(ServerPlayer jogador, SessaoDownloadVideo sessao) {
        Alvo alvo = alvos.get(jogador.getUUID());
        if (alvo == null || alvo.terminado) return;
        if (sessao.tipo().equals("mp4")) alvo.enviadoVideo = sessao.totalBytesEnviados();
        else alvo.enviadoAudio = sessao.totalBytesEnviados();

        long total = Math.max(1, bytesTotaisPorAlvo());
        int percentual = (int) Math.min(100, (alvo.enviadoVideo + alvo.enviadoAudio) * 100 / total);
        int marco = percentual / 25 * 25;
        if (marco > 0 && marco < 100 && marco > alvo.marcoEnvio) {
            alvo.marcoEnvio = marco;
            if (detalhado) {
                mensagemExecutor("§7    " + nomeDe(alvo) + ": enviando §a" + marco + "% §7("
                        + GerenciadorVideoServidor.tamanhoLegivel(alvo.enviadoVideo + alvo.enviadoAudio) + " de "
                        + GerenciadorVideoServidor.tamanhoLegivel(total) + ")");
            }
        }
        if (!detalhado) {
            long soma = 0;
            for (Alvo a : alvos.values()) soma += a.arquivosProntos ? total : Math.min(total, a.enviadoVideo + a.enviadoAudio);
            int media = (int) (soma * 100 / (total * alvos.size()));
            int marcoMedia = media / 25 * 25;
            if (marcoMedia > 0 && marcoMedia < 100 && marcoMedia > marcoMediaEnvio) {
                marcoMediaEnvio = marcoMedia;
                mensagemExecutor("§7    Envio: média de §a" + marcoMedia + "% §7entre os " + alvos.size() + " jogadores ("
                        + contar(a -> a.arquivosProntos) + " já terminaram)");
            }
        }
    }

    public synchronized void envioConcluido(ServerPlayer jogador, SessaoDownloadVideo sessao) {
        Alvo alvo = alvos.get(jogador.getUUID());
        if (alvo == null || alvo.arquivosProntos) return;
        if (sessao.tipo().equals("mp4")) alvo.videoOk = true;
        else alvo.audioOk = true;
        if (sessao.jaTinhaCache()) alvo.jaTinha = true;
        if (!alvo.videoOk || (bytesAudio > 0 && !alvo.audioOk)) return;

        alvo.arquivosProntos = true;
        alvo.terminado = true;
        String detalhe = alvo.jaTinha ? "já tinha a cutscene em cache" : "recebeu e verificou os arquivos (" + duracao(alvo.inicioMillis) + ")";
        mensagemExecutor("§a    " + nomeDe(alvo) + ": " + detalhe);
        verificarResumo();
    }

    public synchronized void envioFalhou(ServerPlayer jogador, String motivo) {
        falhar(jogador, "envio dos arquivos", motivo);
    }

    public synchronized void avisoAudio(ServerPlayer jogador, String motivo) {
        Alvo alvo = alvos.get(jogador.getUUID());
        if (alvo == null) return;
        alvo.audioOk = true;
        mensagemExecutor("§e    " + nomeDe(alvo) + ": o áudio não foi entregue (" + motivo + "). O vídeo pode tocar sem som.");
        if (alvo.videoOk && !alvo.arquivosProntos) {
            alvo.arquivosProntos = true;
            alvo.terminado = true;
            mensagemExecutor("§a    " + nomeDe(alvo) + ": recebeu e verificou o vídeo (" + duracao(alvo.inicioMillis) + ")");
            verificarResumo();
        }
    }

    public synchronized void tocarIgnorado(ServerPlayer jogador, String motivo) {
        falhar(jogador, "verificação do download", motivo);
    }

    public synchronized void tocarEnviado(ServerPlayer jogador, String observacao) {
        Alvo alvo = alvos.get(jogador.getUUID());
        if (alvo == null) return;
        anunciarFase(2, "Os jogadores estão preparando a cutscene (decodificação do vídeo no PC de cada um)...");
        if (observacao != null) mensagemExecutor("§e    " + nomeDe(alvo) + ": " + observacao);
        else if (detalhado) mensagemExecutor("§7    " + nomeDe(alvo) + ": download confirmado, comando de tocar enviado");
    }

    private void falhar(ServerPlayer jogador, String fase, String motivo) {
        Alvo alvo = alvos.get(jogador.getUUID());
        if (alvo == null || alvo.terminado) return;
        alvo.terminado = true;
        alvo.falhou = true;
        alvo.falha = motivo;
        alvo.faseDaFalha = fase;
        mensagemExecutor("§c    " + nomeDe(alvo) + ": falhou na fase de " + fase + ": " + motivo);
        verificarResumo();
    }

    private synchronized void statusDoCliente(ServerPlayer jogador, PacoteStatusCutscene pacote) {
        Alvo alvo = alvos.get(jogador.getUUID());
        if (alvo == null || alvo.terminado) return;
        switch (pacote.fase()) {
            case PacoteStatusCutscene.FASE_PREPARANDO -> {
                anunciarFase(2, "Os jogadores estão preparando a cutscene (decodificação do vídeo no PC de cada um)...");
                mensagemExecutorSeDetalhado("§7    " + nomeDe(alvo) + ": começou a decodificar o vídeo");
            }
            case PacoteStatusCutscene.FASE_PROGRESSO -> {
                int marco = pacote.percentual() / 25 * 25;
                if (marco > 0 && marco < 100 && marco > alvo.marcoPreparo) {
                    alvo.marcoPreparo = marco;
                    mensagemExecutorSeDetalhado("§7    " + nomeDe(alvo) + ": preparando §a" + marco + "%");
                    if (!detalhado) {
                        long soma = 0;
                        for (Alvo a : alvos.values()) soma += a.terminado ? 100 : a.marcoPreparo;
                        int media = (int) (soma / alvos.size());
                        int marcoMedia = media / 25 * 25;
                        if (marcoMedia > 0 && marcoMedia > marcoMediaPreparo) {
                            marcoMediaPreparo = marcoMedia;
                            mensagemExecutor("§7    Preparo: média de §a" + marcoMedia + "% §7entre os jogadores");
                        }
                    }
                }
            }
            case PacoteStatusCutscene.FASE_REPRODUZINDO -> {
                alvo.terminado = true;
                anunciarFase(3, "Cutscene em reprodução.");
                mensagemExecutorSeDetalhado("§a    " + nomeDe(alvo) + ": reproduzindo (" + duracao(alvo.inicioMillis) + " desde o comando)");
                verificarResumo();
            }
            case PacoteStatusCutscene.FASE_FALHA -> falhar(jogador, "preparo no cliente", pacote.mensagem());
            default -> { }
        }
    }

    private void mensagemExecutorSeDetalhado(String texto) {
        if (detalhado) mensagemExecutor(texto);
    }

    private int contar(Predicate<Alvo> filtro) {
        int n = 0;
        for (Alvo a : alvos.values()) if (filtro.test(a)) n++;
        return n;
    }

    private void verificarResumo() {
        if (resumoEnviado) return;
        for (Alvo a : alvos.values()) if (!a.terminado) return;
        enviarResumo(false);
    }

    private synchronized void encerrarPorTempo() {
        if (resumoEnviado) return;
        enviarResumo(true);
    }

    private void enviarResumo(boolean porTempo) {
        resumoEnviado = true;
        for (UUID uuid : alvos.keySet()) ATIVOS_TOCAR.remove(uuid, this);

        int ok = contar(a -> a.terminado && !a.falhou);
        int falhas = contar(a -> a.falhou);
        int semResposta = contar(a -> !a.terminado);
        StringBuilder sb = new StringBuilder();
        if (modo == Modo.BAIXAR) {
            int jaTinham = contar(a -> a.jaTinha);
            anunciarFase(3, "Resumo do download.");
            sb.append("§a[Resumo] Download da cutscene §6").append(nome).append("§a: ").append(ok).append(" concluído(s)");
            if (jaTinham > 0) sb.append(" (").append(jaTinham).append(" já tinham em cache)");
            sb.append(", ").append(falhas).append(" com falha");
        } else {
            sb.append("§a[Resumo] Cutscene §6").append(nome).append("§a: ").append(ok).append(" reproduzindo, ").append(falhas).append(" com falha");
        }
        if (semResposta > 0) sb.append(", ").append(semResposta).append(" sem resposta");
        sb.append(". Tempo total: ").append(duracao(inicioMillis)).append(".");
        mensagemExecutor(sb.toString());

        if (modo == Modo.BAIXAR && falhas == 0 && semResposta == 0) {
            mensagemExecutor("§a    Todos baixaram. Já pode usar /aelysium cutscene tocar " + nome + " ...");
        }
        if (porTempo && semResposta > 0) {
            StringBuilder nomes = new StringBuilder();
            for (Alvo a : alvos.values()) {
                if (!a.terminado) nomes.append(nomes.length() > 0 ? ", " : "").append(nomeDe(a));
            }
            mensagemExecutor("§e    Sem resposta de: " + nomes + ". Pode ser um cliente sem o mod atualizado ou uma conexão lenta.");
        }
        for (Alvo a : alvos.values()) {
            if (a.falhou) mensagemExecutor("§c    - " + nomeDe(a) + ": " + a.faseDaFalha + " (" + a.falha + ")");
        }
    }

    private void anunciarFase(int fase, String texto) {
        if (fase <= faseAnunciada) return;
        faseAnunciada = fase;
        mensagemExecutor("§e[" + fase + "/" + TOTAL_FASES + "] §f" + texto);
    }

    private String nomeDe(Alvo alvo) {
        return alvo.jogador.getName().getString();
    }

    private void mensagemExecutor(String texto) {
        servidor.execute(() -> origem.sendSuccess(() -> Component.literal(texto), false));
    }

    private static String duracao(long inicioMillis) {
        double segundos = (System.currentTimeMillis() - inicioMillis) / 1000.0;
        return String.format(java.util.Locale.ROOT, "%.1f s", segundos);
    }
}
