package net.aelysium.aelysiummod.sistemas.rp.morte.cliente;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)

public class DesenhoVinhetaMorte {

    private static final int VINHETA_COR_RGB = 0x4A0000;
    private static final float VINHETA_TAMANHO_BASE = 90f;
    private static final float VINHETA_TAMANHO_BUMP = 160f;
    private static final float VINHETA_ALPHA_BASE = 0.35f;
    private static final float VINHETA_ALPHA_BUMP = 0.70f;

    static void desenharVinheta(GuiGraphics gfx, int screenW, int screenH, long decorrido) {
        float tamanho;
        float alpha;

        if (decorrido < TemporizacaoSobreposicaoMorte.POP_MS) {
            float t = decorrido / (float) TemporizacaoSobreposicaoMorte.POP_MS;
            tamanho = VINHETA_TAMANHO_BASE * t;
            alpha = VINHETA_ALPHA_BASE * t;

        } else if (decorrido < TemporizacaoSobreposicaoMorte.POP_MS + TemporizacaoSobreposicaoMorte.BUMP_MS) {
            float u = UtilitariosSobreposicaoMorte.smoothstep((decorrido - TemporizacaoSobreposicaoMorte.POP_MS) / (float) TemporizacaoSobreposicaoMorte.BUMP_MS);
            tamanho = VINHETA_TAMANHO_BASE + (VINHETA_TAMANHO_BUMP - VINHETA_TAMANHO_BASE) * u;
            alpha = VINHETA_ALPHA_BASE + (VINHETA_ALPHA_BUMP - VINHETA_ALPHA_BASE) * u;

        } else if (decorrido < TemporizacaoSobreposicaoMorte.POP_MS + TemporizacaoSobreposicaoMorte.BUMP_MS + TemporizacaoSobreposicaoMorte.DECAY_MS + TemporizacaoSobreposicaoMorte.HOLD_MS) {
            tamanho = VINHETA_TAMANHO_BUMP;
            alpha = VINHETA_ALPHA_BUMP;

        } else {
            float t = (decorrido - TemporizacaoSobreposicaoMorte.POP_MS - TemporizacaoSobreposicaoMorte.BUMP_MS - TemporizacaoSobreposicaoMorte.DECAY_MS - TemporizacaoSobreposicaoMorte.HOLD_MS) / (float) TemporizacaoSobreposicaoMorte.FADEOUT_MS;
            tamanho = VINHETA_TAMANHO_BUMP;
            alpha = VINHETA_ALPHA_BUMP * (1.0f - Math.min(1.0f, t));
        }

        if (alpha <= 0f || tamanho <= 0f) return;

        int faixa = (int) tamanho;
        int corOpaca = ((int) (alpha * 255f) << 24) | VINHETA_COR_RGB;
        int corTransparente = VINHETA_COR_RGB & 0x00FFFFFF;

        RenderSystem.enableBlend();
        gfx.fillGradient(0, 0, screenW, faixa, corOpaca, corTransparente);
        gfx.fillGradient(0, screenH - faixa, screenW, screenH, corTransparente, corOpaca);
        desenharGradienteHorizontal(gfx, 0, 0, faixa, screenH, corOpaca, corTransparente);
        desenharGradienteHorizontal(gfx, screenW - faixa, 0, screenW, screenH, corTransparente, corOpaca);
    }

    private static void desenharGradienteHorizontal(GuiGraphics gfx, int x1, int y1, int x2, int y2, int corEsquerda, int corDireita) {
        VertexConsumer buffer = gfx.bufferSource().getBuffer(RenderType.gui());
        var matrix = gfx.pose().last().pose();
        buffer.addVertex(matrix, x1, y1, 0).setColor(corEsquerda);
        buffer.addVertex(matrix, x1, y2, 0).setColor(corEsquerda);
        buffer.addVertex(matrix, x2, y2, 0).setColor(corDireita);
        buffer.addVertex(matrix, x2, y1, 0).setColor(corDireita);
    }
}
