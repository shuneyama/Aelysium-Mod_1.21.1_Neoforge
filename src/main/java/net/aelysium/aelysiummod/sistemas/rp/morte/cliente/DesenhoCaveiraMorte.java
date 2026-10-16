package net.aelysium.aelysiummod.sistemas.rp.morte.cliente;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)

public class DesenhoCaveiraMorte {

    private static final ResourceLocation CAVEIRA_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/gui/morte/caveira_morte.png");
    private static final int CAVEIRA_TAMANHO_NATIVO = 32;
    private static final int CAVEIRA_TAMANHO_TELA = 96;

    private static final float ESCALA_INICIAL = 4.5f;
    private static final float ESCALA_BUMP = 1.15f;

    private static final float SHAKE_FRACO = 2.5f;
    private static final float SHAKE_HOLD = 1.3f;
    private static final float SHAKE_FORTE = 11.0f;
    private static final float ROTACAO_GRAUS_POR_AMP = 1.1f;

    private static final int BRILHO_COR_RGB = 0x1A0000;
    private static final float BRILHO_ESCALA_MULT = 4.2f;
    private static final float BRILHO_ALPHA_MAX = 0.65f;

    private static final ResourceLocation BRILHO_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "dynamic/morte_brilho");
    private static final int BRILHO_TAMANHO_NATIVO = 128;
    private static boolean brilhoRegistrado = false;

    static void garantirBrilhoRegistrado(Minecraft mc) {
        if (brilhoRegistrado) return;
        brilhoRegistrado = true;

        int tam = BRILHO_TAMANHO_NATIVO;
        NativeImage imagem = new NativeImage(tam, tam, false);
        float centro = (tam - 1) / 2f;
        float raioMax = tam / 2f;

        for (int y = 0; y < tam; y++) {
            for (int x = 0; x < tam; x++) {
                float dx = x - centro;
                float dy = y - centro;
                float dist = (float) Math.sqrt(dx * dx + dy * dy) / raioMax;
                float falloff = 1.0f - UtilitariosSobreposicaoMorte.smoothstep(Math.min(1.0f, dist));
                int alpha = Math.round(falloff * 255f);
                imagem.setPixelRGBA(x, y, FastColor.ABGR32.color(alpha, 255, 255, 255));
            }
        }

        mc.getTextureManager().register(BRILHO_TEXTURE, new DynamicTexture(imagem));
    }

    static void desenharCaveira(GuiGraphics gfx, int centerX, int centerY, long decorrido) {
        float escala;
        float alpha;
        float shakeAmp;

        if (decorrido < TemporizacaoSobreposicaoMorte.POP_MS) {
            float t = decorrido / (float) TemporizacaoSobreposicaoMorte.POP_MS;
            float easeOut = 1.0f - (float) Math.pow(1.0f - t, 3.0);
            escala = ESCALA_INICIAL - (ESCALA_INICIAL - 1.0f) * easeOut;
            alpha = Math.min(1.0f, t / 0.3f);
            shakeAmp = SHAKE_FRACO * t;

        } else if (decorrido < TemporizacaoSobreposicaoMorte.POP_MS + TemporizacaoSobreposicaoMorte.BUMP_MS) {
            float u = (decorrido - TemporizacaoSobreposicaoMorte.POP_MS) / (float) TemporizacaoSobreposicaoMorte.BUMP_MS;
            float eased = UtilitariosSobreposicaoMorte.smoothstep(u);
            escala = 1.0f + (ESCALA_BUMP - 1.0f) * eased;
            alpha = 1.0f;
            shakeAmp = SHAKE_FRACO + (SHAKE_FORTE - SHAKE_FRACO) * eased;

        } else if (decorrido < TemporizacaoSobreposicaoMorte.POP_MS + TemporizacaoSobreposicaoMorte.BUMP_MS + TemporizacaoSobreposicaoMorte.DECAY_MS) {
            float u = (decorrido - TemporizacaoSobreposicaoMorte.POP_MS - TemporizacaoSobreposicaoMorte.BUMP_MS) / (float) TemporizacaoSobreposicaoMorte.DECAY_MS;
            float eased = UtilitariosSobreposicaoMorte.smoothstep(u);
            escala = ESCALA_BUMP - (ESCALA_BUMP - 1.0f) * eased;
            alpha = 1.0f;
            shakeAmp = SHAKE_FORTE - (SHAKE_FORTE - SHAKE_HOLD) * eased;

        } else if (decorrido < TemporizacaoSobreposicaoMorte.POP_MS + TemporizacaoSobreposicaoMorte.BUMP_MS + TemporizacaoSobreposicaoMorte.DECAY_MS + TemporizacaoSobreposicaoMorte.HOLD_MS) {
            escala = 1.0f;
            alpha = 1.0f;
            shakeAmp = SHAKE_HOLD;

        } else {
            float t = (decorrido - TemporizacaoSobreposicaoMorte.POP_MS - TemporizacaoSobreposicaoMorte.BUMP_MS - TemporizacaoSobreposicaoMorte.DECAY_MS - TemporizacaoSobreposicaoMorte.HOLD_MS) / (float) TemporizacaoSobreposicaoMorte.FADEOUT_MS;
            float tClamped = Math.min(1.0f, t);
            escala = 1.0f;
            alpha = 1.0f - tClamped;
            shakeAmp = SHAKE_HOLD * (1.0f - UtilitariosSobreposicaoMorte.smoothstep(tClamped));
        }

        float shakeX = 0f;
        float shakeY = 0f;
        float shakeRot = 0f;
        if (shakeAmp > 0f) {
            float tSec = decorrido / 1000f;
            shakeX = shakeAmp * (float) (Math.sin(tSec * 37.0) * 0.6 + Math.sin(tSec * 53.0 + 1.3) * 0.4);
            shakeY = shakeAmp * (float) (Math.sin(tSec * 41.0 + 0.7) * 0.6 + Math.sin(tSec * 59.0 + 2.1) * 0.4);
            shakeRot = shakeAmp * ROTACAO_GRAUS_POR_AMP * (float) Math.sin(tSec * 29.0 + 0.4);
        }

        var pose = gfx.pose();
        pose.pushPose();
        pose.translate(centerX + shakeX, centerY + shakeY, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(shakeRot));

        if (EstadoSobreposicaoMorte.habilitando) {
            desenharBrilho(gfx, pose, escala, alpha);
        }
        desenharCaveiraSprite(gfx, pose, escala, alpha);

        pose.popPose();
    }

    private static void desenharBrilho(GuiGraphics gfx, PoseStack pose, float escala, float alpha) {
        float escalaTotal = escala * (CAVEIRA_TAMANHO_TELA * BRILHO_ESCALA_MULT / (float) BRILHO_TAMANHO_NATIVO);

        pose.pushPose();
        pose.scale(escalaTotal, escalaTotal, 1f);

        RenderSystem.enableBlend();
        int metade = BRILHO_TAMANHO_NATIVO / 2;
        gfx.setColor(
                ((BRILHO_COR_RGB >> 16) & 0xFF) / 255f,
                ((BRILHO_COR_RGB >> 8) & 0xFF) / 255f,
                (BRILHO_COR_RGB & 0xFF) / 255f,
                alpha * BRILHO_ALPHA_MAX
        );
        gfx.blit(BRILHO_TEXTURE, -metade, -metade, 0, 0f, 0f,
                BRILHO_TAMANHO_NATIVO, BRILHO_TAMANHO_NATIVO, BRILHO_TAMANHO_NATIVO, BRILHO_TAMANHO_NATIVO);
        gfx.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        pose.popPose();
    }

    private static void desenharCaveiraSprite(GuiGraphics gfx, PoseStack pose, float escala, float alpha) {
        float escalaTotal = escala * (CAVEIRA_TAMANHO_TELA / (float) CAVEIRA_TAMANHO_NATIVO);

        pose.pushPose();
        pose.scale(escalaTotal, escalaTotal, 1f);

        RenderSystem.enableBlend();
        gfx.setColor(1.0f, 1.0f, 1.0f, alpha);
        int metade = CAVEIRA_TAMANHO_NATIVO / 2;
        gfx.blit(CAVEIRA_TEXTURE, -metade, -metade, 0, 0f, 0f,
                CAVEIRA_TAMANHO_NATIVO, CAVEIRA_TAMANHO_NATIVO, CAVEIRA_TAMANHO_NATIVO, CAVEIRA_TAMANHO_NATIVO);
        gfx.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        pose.popPose();
    }
}
