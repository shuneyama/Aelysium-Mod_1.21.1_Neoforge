package net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Random;

import static net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura.PrimitivasDesenhoMorte.*;

@OnlyIn(Dist.CLIENT)
public final class DesenhoFrasesMorte {

    private static final int COPIAS_BORRAO = 6;

    private DesenhoFrasesMorte() {
    }

    public static void desenharFraseOndulada(GuiGraphics g, Font font, String frase, float cx, float cy, float escala,
                                             int rgb, float alpha, float borrao, float onda, long t) {
        if (alpha < 0.03f) return;
        g.flush();
        float largura = font.width(frase);
        if (borrao > 0.01f) {
            for (int k = 1; k <= COPIAS_BORRAO; k++) {
                float deslocamento = borrao * k / COPIAS_BORRAO;
                float alphaCopia = alpha * 0.22f * (1f - (float) k / (COPIAS_BORRAO + 1));
                desenharLetras(g, font, frase, cx - deslocamento, cy, escala, largura, rgb, alphaCopia, onda, t);
                desenharLetras(g, font, frase, cx + deslocamento * 0.6f, cy, escala, largura, rgb, alphaCopia * 0.7f, onda, t);
            }
        }
        desenharLetras(g, font, frase, cx, cy, escala, largura, rgb, alpha, onda, t);
        g.flush();
    }

    private static void desenharLetras(GuiGraphics g, Font font, String frase, float cx, float cy, float escala,
                                       float largura, int rgb, float alpha, float onda, long t) {
        if (alpha < 0.03f) return;
        int cor = argb(alpha, rgb);
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().scale(escala, escala, 1f);
        float x = -largura / 2f;
        for (int i = 0; i < frase.length(); i++) {
            String letra = String.valueOf(frase.charAt(i));
            float deslocY = (float) Math.sin(i * 0.8f + t * 0.005f) * onda;
            float esticoY = 1f + (float) Math.sin(i * 1.7f + t * 0.003f) * onda * 0.08f;
            g.pose().pushPose();
            g.pose().translate(x, deslocY - 4f, 0);
            g.pose().scale(1f, esticoY, 1f);
            g.drawString(font, letra, 0, 0, cor, false);
            g.pose().popPose();
            x += font.width(letra);
        }
        g.pose().popPose();
    }

    public static void desenharSpam(GuiGraphics g, Font font, String frase, int w, int h, long semente, int quantidade, float alpha) {
        if (alpha < 0.03f) return;
        Random r = new Random(semente);
        g.flush();
        for (int i = 0; i < quantidade; i++) {
            float escala = 1.5f + r.nextFloat() * 1.5f;
            float x = r.nextFloat() * w - font.width(frase) * escala * 0.3f;
            float y = r.nextFloat() * h;
            int rgb = r.nextFloat() < 0.85f ? 0xC8101A : 0xFF3030;
            g.pose().pushPose();
            g.pose().translate(x, y, 0);
            g.pose().scale(escala, escala, 1f);
            g.drawString(font, frase, 0, 0, argb(alpha * (0.55f + r.nextFloat() * 0.45f), rgb), false);
            g.pose().popPose();
        }
        g.flush();
    }
}
