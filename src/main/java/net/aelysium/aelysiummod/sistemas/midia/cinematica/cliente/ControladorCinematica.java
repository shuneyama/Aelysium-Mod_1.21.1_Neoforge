package net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteStatusCutscene;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

import java.nio.file.Files;
import java.nio.file.Path;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class ControladorCinematica {

    public static class EstadoRender {
        public boolean playingVideo;
        public float fadeAlpha;
    }


    private static volatile boolean ativo = false;
    private static int fadeInTicks  = 20;
    private static int fadeOutTicks = 20;
    private static long startNanos  = 0;

    private static ReprodutorMidia mediaPlayer  = null;
    private static float pontoFimTicks = -1;

    private static net.minecraft.client.gui.screens.Screen screenAnterior = null;
    private static double volumeMasterSalvo = -1;


    private static Path preDecodedTempFile = null;
    private static String nomeAtual = "";
    private static int marcoPreparoEnviado = 0;
    private static float ultimoProgressoPreparo = -1f;
    private static long ultimaMudancaProgressoNanos = 0;
    private static boolean primeiroQuadroExibido = false;
    private static final long LIMITE_PREPARO_PARADO_NS = 30_000_000_000L;
    private static final long LIMITE_TELA_PRETA_MS = 8000L;


    private static PreDecodificadorFfmpeg preDecoder = null;


    private static Path preDecodedPendingFile = null;



    private static boolean fadendoParaPreto        = false;
    private static long    fadeParaPretoStartNanos  = 0;
    private static final long FADE_PARA_PRETO_NS   = 1_500_000_000L;


    private static String preloadFfmpegCmd;
    private static Path   preloadVideoFile;
    private static int    preloadWidth, preloadHeight;
    private static double preloadFps;
    private static int    preloadFadeIn, preloadFadeOut;
    private static long   preloadStartDelay;



    public static boolean estaAtivo()      { return ativo; }
    public static boolean estaCarregando() { return preDecoder != null || fadendoParaPreto; }

    private static boolean bloqueandoTela() { return ativo || fadendoParaPreto; }


    public static synchronized void iniciarPreload(String nome, int fadeIn, int fadeOut, long startDelayMillis) {
        parar();
        nomeAtual = nome;
        marcoPreparoEnviado = 0;
        ultimoProgressoPreparo = -1f;
        ultimaMudancaProgressoNanos = System.nanoTime();

        String ffmpegCmd = InicializadorFfmpeg.getCommand();
        if (ffmpegCmd == null) {
            falhar(Component.translatable("gui.aelysiummod.cinematica.ffmpeg_nao_encontrado"));
            return;
        }

        Path videoFile = GerenciadorVideoCliente.arquivoCache(nome, "mp4");
        if (!Files.isRegularFile(videoFile)) {
            falhar(Component.translatable("gui.aelysiummod.cinematica.video_nao_encontrado_local"));
            return;
        }

        SondaMidia.Informacoes info;
        try {
            info = SondaMidia.probe(ffmpegCmd, videoFile);
        } catch (Exception e) {
            falhar(Component.translatableEscape("gui.aelysiummod.cinematica.erro_analisar_video", e.getMessage()));
            return;
        }

        int outWidth  = info.width;
        int outHeight = info.height;
        if (outHeight > 720) {
            outWidth  = (info.width * 720 / info.height) & ~1;
            outHeight = 720;
        }

        int totalFrames = (int)(info.durationSeconds * Math.min(info.fps, 30.0));

        long bytesNecessarios = (long) outWidth * outHeight * 4L * Math.max(1, totalFrames);
        try {
            long livre = Files.getFileStore(Path.of(System.getProperty("java.io.tmpdir"))).getUsableSpace();
            if (livre < bytesNecessarios + bytesNecessarios / 20) {
                falhar(Component.literal("espaço insuficiente na pasta temporária: o vídeo decodificado precisa de "
                        + formatarBytes(bytesNecessarios) + " e há " + formatarBytes(livre) + " livres"));
                return;
            }
        } catch (Exception ignored) {}

        PreDecodificadorFfmpeg decoder = new PreDecodificadorFfmpeg();
        try {
            decoder.start(ffmpegCmd, videoFile, outWidth, outHeight, totalFrames);
        } catch (Exception e) {
            decoder.close();
            falhar(Component.translatableEscape("gui.aelysiummod.cinematica.erro_iniciar_predecode", e.getMessage()));
            return;
        }

        preDecoder        = decoder;
        preloadFfmpegCmd  = ffmpegCmd;
        preloadVideoFile  = videoFile;
        preloadWidth      = outWidth;
        preloadHeight     = outHeight;
        preloadFps        = info.fps;
        preloadFadeIn     = fadeIn;
        preloadFadeOut    = fadeOut;
        preloadStartDelay = startDelayMillis;

        relatar(PacoteStatusCutscene.FASE_PREPARANDO, 0, "");
    }

    public static synchronized void parar() {
        ativo              = false;
        fadendoParaPreto   = false;
        fadeParaPretoStartNanos = 0;

        if (preDecoder != null) {
            preDecoder.close();
            preDecoder = null;
        }

        if (preDecodedPendingFile != null) {
            try { Files.deleteIfExists(preDecodedPendingFile); } catch (Exception ignored) {}
            preDecodedPendingFile = null;
        }

        if (mediaPlayer != null) {
            mediaPlayer.close();
            mediaPlayer = null;
        }

        if (preDecodedTempFile != null) {
            try { Files.deleteIfExists(preDecodedTempFile); } catch (Exception ignored) {}
            preDecodedTempFile = null;
        }

        pontoFimTicks = -1;

        Minecraft mc = Minecraft.getInstance();

        if (volumeMasterSalvo >= 0) {
            mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(volumeMasterSalvo);
            volumeMasterSalvo = -1;
        }

        if (mc.screen instanceof TelaCinematica cs) {
            cs.liberarTextura();
            mc.setScreen(screenAnterior);
        }
        screenAnterior = null;
    }

    public static EstadoRender calcularEstado() {
        if (!ativo) return null;

        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;
        EstadoRender e = new EstadoRender();

        if (elapsedMillis < 0) {
            e.fadeAlpha = 0f;
            e.playingVideo = false;
            return e;
        }

        if (!primeiroQuadroExibido) {
            if (mediaPlayer != null && mediaPlayer.currentFrame() != null) {
                primeiroQuadroExibido = true;
            } else if (elapsedMillis > fadeInTicks * 50L + LIMITE_TELA_PRETA_MS) {
                falhar(Component.literal("tela preta: nenhum quadro de vídeo foi exibido em " + (LIMITE_TELA_PRETA_MS / 1000) + " s"));
                return null;
            }
        }

        float elapsedTicks = (elapsedMillis / 1000.0f) * 20.0f;

        if (elapsedTicks < fadeInTicks) {
            e.fadeAlpha = elapsedTicks / Math.max(1f, (float) fadeInTicks);
            e.playingVideo = true;
            return e;
        }

        float ticksVideo = elapsedTicks - fadeInTicks;

        if (pontoFimTicks < 0 && mediaPlayer != null && mediaPlayer.isFinished()) {
            pontoFimTicks = ticksVideo;
        }

        if (pontoFimTicks < 0) {
            e.fadeAlpha = 1f;
            e.playingVideo = true;
            return e;
        }

        float ticksFadeOut = ticksVideo - pontoFimTicks;
        if (ticksFadeOut < fadeOutTicks) {
            e.fadeAlpha = 1f - (ticksFadeOut / Math.max(1f, (float) fadeOutTicks));
            e.playingVideo = true;
            return e;
        }

        return null;
    }

    public static QuadroVideo obterFrameAtual() {
        if (mediaPlayer == null) return null;
        return mediaPlayer.currentFrame();
    }




    private static synchronized void iniciarPlayback() {
        Path decodedFile = preDecodedPendingFile;
        if (decodedFile == null) return;
        preDecodedPendingFile = null;

        ReprodutorMidia player = new ReprodutorMidia();
        try {
            player.playFromPreDecoded(preloadFfmpegCmd, preloadVideoFile, decodedFile,
                    preloadWidth, preloadHeight, preloadFps);
        } catch (Exception e) {
            player.close();
            try { Files.deleteIfExists(decodedFile); } catch (Exception ignored) {}
            falhar(Component.translatableEscape("gui.aelysiummod.cinematica.erro_iniciar_cutscene", e.getMessage()));
            return;
        }

        preDecodedTempFile = decodedFile;
        mediaPlayer        = player;
        fadeInTicks        = preloadFadeIn;
        fadeOutTicks       = preloadFadeOut;
        startNanos         = System.nanoTime() + Math.max(0L, preloadStartDelay) * 1_000_000L;
        pontoFimTicks      = -1;
        primeiroQuadroExibido = false;
        ativo              = true;
        relatar(PacoteStatusCutscene.FASE_REPRODUZINDO, 100, "");

        Minecraft mc = Minecraft.getInstance();

        var masterOpt = mc.options.getSoundSourceOptionInstance(SoundSource.MASTER);
        volumeMasterSalvo = masterOpt.get();
        masterOpt.set(0.0);
        mc.getSoundManager().stop();

        screenAnterior = mc.screen;
        mc.setScreen(new TelaCinematica());
    }

    private static void renderLoadingDots(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();


        float cycle = (System.nanoTime() % 900_000_000L) / 900_000_000f;

        int dotSize = 5;
        int dotGap  = 5;
        int jump    = 7;
        int margin  = 16;
        int baseX   = sw - (dotSize * 3 + dotGap * 2) - margin;
        int baseY   = sh - dotSize - margin;

        for (int i = 0; i < 3; i++) {
            float windowStart = i / 3f;
            int offsetY = 0;
            if (cycle >= windowStart && cycle < windowStart + 1f / 3f) {
                float t = (cycle - windowStart) * 3f;
                offsetY = (int)(Math.sin(t * Math.PI) * jump);
            }
            int x = baseX + i * (dotSize + dotGap);
            int y = baseY - offsetY;
            graphics.fill(x, y, x + dotSize, y + dotSize, 0xCCFFFFFF);
        }
    }

    private static String formatarBytes(long bytes) {
        if (bytes < 1024L * 1024L * 1024L) return String.format(java.util.Locale.ROOT, "%.0f MB", bytes / (1024.0 * 1024.0));
        return String.format(java.util.Locale.ROOT, "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private static void reportarProgressoPreparo(int percentual) {
        int marco = percentual / 25 * 25;
        if (marco > 0 && marco < 100 && marco > marcoPreparoEnviado) {
            marcoPreparoEnviado = marco;
            relatar(PacoteStatusCutscene.FASE_PROGRESSO, marco, "");
        }
    }

    private static void relatar(int fase, int percentual, String mensagem) {
        if (Minecraft.getInstance().getConnection() == null || nomeAtual.isEmpty()) return;
        PacketDistributor.sendToServer(new PacoteStatusCutscene(nomeAtual, fase, percentual, mensagem == null ? "" : mensagem));
    }

    private static void falhar(Component msg) {
        relatar(PacoteStatusCutscene.FASE_FALHA, 0, msg.getString());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderHudLayer(RenderGuiLayerEvent.Pre event) {
        if (bloqueandoTela()) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderGui(RenderGuiEvent.Pre event) {



        if (ativo) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        if (fadendoParaPreto) {
            float alpha = Math.min(1f, (float)(System.nanoTime() - fadeParaPretoStartNanos) / FADE_PARA_PRETO_NS);
            Minecraft mc = Minecraft.getInstance();
            int sw = mc.getWindow().getGuiScaledWidth();
            int sh = mc.getWindow().getGuiScaledHeight();
            event.getGuiGraphics().fill(0, 0, sw, sh, (int)(alpha * 255) << 24);
            if (alpha >= 1f) {
                fadendoParaPreto = false;
                iniciarPlayback();
            }
            return;
        }

        if (preDecoder == null) return;

        if (preDecoder.isFailed()) {
            falhar(Component.translatableEscape("gui.aelysiummod.cinematica.falha_predecode", preDecoder.getFailReason()));
            preDecoder.close();
            preDecoder = null;
            return;
        }

        renderLoadingDots(event.getGuiGraphics());
        reportarProgressoPreparo((int) (preDecoder.getProgress() * 100));

        float progressoAtual = preDecoder.getProgress();
        long agoraNanos = System.nanoTime();
        if (progressoAtual != ultimoProgressoPreparo) {
            ultimoProgressoPreparo = progressoAtual;
            ultimaMudancaProgressoNanos = agoraNanos;
        } else if (!preDecoder.isDone() && agoraNanos - ultimaMudancaProgressoNanos > LIMITE_PREPARO_PARADO_NS) {
            falhar(Component.literal("o preparo do vídeo ficou parado por " + (LIMITE_PREPARO_PARADO_NS / 1_000_000_000L)
                    + " s em " + (int) (progressoAtual * 100) + "%"));
            preDecoder.close();
            preDecoder = null;
            return;
        }

        if (preDecoder.isDone()) {

            Path saida = preDecoder.takeOutputFile();
            long tamanhoSaida = 0;
            try { tamanhoSaida = saida == null ? 0 : Files.size(saida); } catch (Exception ignored) {}
            preDecoder.close();
            preDecoder = null;
            if (tamanhoSaida < (long) preloadWidth * preloadHeight * 4L) {
                if (saida != null) try { Files.deleteIfExists(saida); } catch (Exception ignored) {}
                falhar(Component.literal("o ffmpeg terminou sem gerar nenhum quadro de vídeo"));
                return;
            }
            preDecodedPendingFile   = saida;
            fadendoParaPreto        = true;
            fadeParaPretoStartNanos = System.nanoTime();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        if (bloqueandoTela()) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (ativo && !(event.getNewScreen() instanceof TelaCinematica)) {
            event.setCanceled(true);
        }
    }
}
