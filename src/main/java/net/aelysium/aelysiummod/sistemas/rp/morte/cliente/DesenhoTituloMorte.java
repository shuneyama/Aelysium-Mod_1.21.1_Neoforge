package net.aelysium.aelysiummod.sistemas.rp.morte.cliente;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.aelysium.aelysiummod.nucleo.cliente.VolumeSomCliente;

@OnlyIn(Dist.CLIENT)

public class DesenhoTituloMorte {

    private static final String TEXTO_HABILITAR = "A morte não perdoa.";
    private static final String TEXTO_DESABILITAR = "A morte perdoa, por ora.";
    private static final int TEXTO_OFFSET_Y = 70;
    private static final float TEXTO_ESCALA = 1.4f;
    private static final long TEXTO_INICIO_MS = TemporizacaoSobreposicaoMorte.POP_MS + TemporizacaoSobreposicaoMorte.BUMP_MS + TemporizacaoSobreposicaoMorte.DECAY_MS;
    private static final long TEXTO_FADEIN_MS = 250L;
    private static final long TEXTO_FADEOUT_INICIO_MS = TEXTO_INICIO_MS + TemporizacaoSobreposicaoMorte.HOLD_MS;
    private static final float TEXTO_SHAKE_AMP = 0.6f;

    private static final long TEXTO_GLITCH_MS = 140L;
    private static final long TEXTO_GLITCH_PASSO_MS = 18L;
    private static final float TEXTO_GLITCH_OFFSET_MAX = 3.2f;
    private static final int TEXTO_GLITCH_COR = 0xFF1010;

    private static final long[] TEXTO_PISCA_PADRAO = {
            120, 100, 120, 100, 120, 300,
            350, 100, 350, 100, 350, 300,
            120, 100, 120, 100, 120, 700
    };

    private static void tocarBipeSePiscou(boolean aceso) {
        if (aceso && !EstadoSobreposicaoMorte.piscaAcesoAnterior) {
            float volume = 0.5f * (float) VolumeSomCliente.normalizado();
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), 0.6f, volume)
            );
        }
        EstadoSobreposicaoMorte.piscaAcesoAnterior = aceso;
    }

    private static float calcularPiscaAlpha(long tempoDesdeEstavel) {
        long total = 0;
        for (long d : TEXTO_PISCA_PADRAO) total += d;

        long t = tempoDesdeEstavel % total;
        long acumulado = 0;
        for (int i = 0; i < TEXTO_PISCA_PADRAO.length; i++) {
            acumulado += TEXTO_PISCA_PADRAO[i];
            if (t < acumulado) {
                return (i % 2 == 0) ? 1.0f : 0.0f;
            }
        }
        return 1.0f;
    }

    static void desenharTitulo(GuiGraphics gfx, Minecraft mc, int centerX, int centerY, long decorrido) {
        if (decorrido < TEXTO_INICIO_MS) return;

        long fimFadein = TEXTO_INICIO_MS + TEXTO_FADEIN_MS;

        float alpha;
        if (decorrido < fimFadein) {
            alpha = (decorrido - TEXTO_INICIO_MS) / (float) TEXTO_FADEIN_MS;
        } else if (decorrido < TEXTO_FADEOUT_INICIO_MS) {
            alpha = calcularPiscaAlpha(decorrido - fimFadein);
            boolean aceso = alpha > 0.5f;
            if (aceso && !EstadoSobreposicaoMorte.piscaAcesoAnterior) {
                EstadoSobreposicaoMorte.glitchInicioMs = decorrido;
            }
            tocarBipeSePiscou(aceso);
        } else {
            float t = (decorrido - TEXTO_FADEOUT_INICIO_MS) / (float) TemporizacaoSobreposicaoMorte.FADEOUT_MS;
            alpha = 1.0f - Math.min(1.0f, t);
        }
        if (alpha <= 0f) return;

        float corProgresso;
        if (decorrido < TEXTO_FADEOUT_INICIO_MS) {
            corProgresso = 0f;
        } else {
            corProgresso = Math.min(1.0f, (decorrido - TEXTO_FADEOUT_INICIO_MS) / (float) TemporizacaoSobreposicaoMorte.FADEOUT_MS);
        }
        int canal = Math.round(255 * (1.0f - corProgresso));
        int cor = (canal << 16) | (canal << 8) | canal;

        float tSec = decorrido / 1000f;
        float shakeX = TEXTO_SHAKE_AMP * (float) (Math.sin(tSec * 31.0) * 0.6 + Math.sin(tSec * 47.0 + 1.1) * 0.4);
        float shakeY = TEXTO_SHAKE_AMP * (float) (Math.sin(tSec * 43.0 + 0.5) * 0.6 + Math.sin(tSec * 61.0 + 2.4) * 0.4);

        String texto = EstadoSobreposicaoMorte.habilitando ? TEXTO_HABILITAR : TEXTO_DESABILITAR;

        var pose = gfx.pose();
        pose.pushPose();
        pose.translate(centerX + shakeX, centerY - TEXTO_OFFSET_Y + shakeY, 0);
        pose.scale(TEXTO_ESCALA, TEXTO_ESCALA, 1f);

        int largura = mc.font.width(texto);

        boolean glitchAtivo = EstadoSobreposicaoMorte.glitchInicioMs >= 0 && (decorrido - EstadoSobreposicaoMorte.glitchInicioMs) < TEXTO_GLITCH_MS;
        if (glitchAtivo) {
            long passo = (decorrido - EstadoSobreposicaoMorte.glitchInicioMs) / TEXTO_GLITCH_PASSO_MS;
            float glitchX1 = UtilitariosSobreposicaoMorte.hashSalto(passo, 0) * TEXTO_GLITCH_OFFSET_MAX;
            float glitchY1 = UtilitariosSobreposicaoMorte.hashSalto(passo, 1) * (TEXTO_GLITCH_OFFSET_MAX * 0.4f);
            float glitchX2 = UtilitariosSobreposicaoMorte.hashSalto(passo, 2) * TEXTO_GLITCH_OFFSET_MAX;
            float glitchY2 = UtilitariosSobreposicaoMorte.hashSalto(passo, 3) * (TEXTO_GLITCH_OFFSET_MAX * 0.4f);

            int corGlitch = (Math.round(alpha * 200f) << 24) | TEXTO_GLITCH_COR;
            gfx.drawString(mc.font, texto, Math.round(-largura / 2f + glitchX1), Math.round(glitchY1), corGlitch, false);
            gfx.drawString(mc.font, texto, Math.round(-largura / 2f + glitchX2), Math.round(glitchY2), corGlitch, false);
        }

        int corFinal = (Math.round(alpha * 255f) << 24) | cor;
        gfx.drawString(mc.font, texto, -largura / 2, 0, corFinal, true);

        pose.popPose();
    }
}
