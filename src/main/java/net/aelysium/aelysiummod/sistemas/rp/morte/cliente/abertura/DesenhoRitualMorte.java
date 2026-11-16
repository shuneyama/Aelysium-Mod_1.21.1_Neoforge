package net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura;

import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura.PrimitivasDesenhoMorte.*;

@OnlyIn(Dist.CLIENT)
public class DesenhoRitualMorte {

    public static final long DURACAO_DESENHO = 2200;
    private static final int GLIFOS_ANEL = 28;

    private record LinhaGlifo(float x, float y, char[] letras, Style estilo, float escala, long inicio, long duracao,
                              float alpha, int cor) {
    }

    private final Random random;
    private final List<LinhaGlifo> linhas = new ArrayList<>();
    private final char[] anel;

    public DesenhoRitualMorte(Random random) {
        this.random = random;
        this.anel = letrasAleatorias(random, GLIFOS_ANEL);
    }

    public void atualizarGlifos(long t, int w, int h, float densidade) {
        linhas.removeIf(linha -> t > linha.inicio() + linha.duracao());
        if (random.nextFloat() < densidade) {
            float sorteio = random.nextFloat();
            int cor = sorteio < 0.75f ? 0xB3121A : sorteio < 0.95f ? 0xFF2A2A : 0xE8E8E8;
            float[] escalas = {1f, 1.5f, 2f, 2.5f};
            linhas.add(new LinhaGlifo(
                    random.nextFloat() * w - 20,
                    random.nextFloat() * h,
                    letrasAleatorias(random, 6 + random.nextInt(17)),
                    random.nextFloat() < 0.7f ? ESTILO_GLIFO : ESTILO_GLIFO_ILLAGER,
                    escalas[random.nextInt(escalas.length)],
                    t,
                    300 + random.nextInt(1100),
                    0.25f + random.nextFloat() * 0.65f,
                    cor));
        }
        for (LinhaGlifo linha : linhas) {
            for (int i = 0; i < linha.letras().length; i++) {
                if (random.nextFloat() < 0.08f) linha.letras()[i] = letraAleatoria(random);
            }
        }
    }

    public void desenharGlifos(GuiGraphics g, Font font, long t, float alphaGlobal) {
        if (alphaGlobal < 0.01f) return;
        g.flush();
        for (LinhaGlifo linha : linhas) {
            float env = envelope(t, linha.inicio(), linha.inicio() + 80, linha.inicio() + linha.duracao() - 150, linha.inicio() + linha.duracao());
            float alpha = linha.alpha() * env * alphaGlobal;
            if (random.nextFloat() < 0.1f) alpha *= 0.3f;
            if (alpha < 0.03f) continue;
            g.pose().pushPose();
            g.pose().translate(linha.x(), linha.y(), 0);
            g.pose().scale(linha.escala(), linha.escala(), 1f);
            g.drawString(font, Component.literal(new String(linha.letras())).withStyle(linha.estilo()), 0, 0, argb(alpha, linha.cor()), false);
            g.pose().popPose();
        }
        g.flush();
    }

    public void desenharCirculo(GuiGraphics g, Font font, int w, int h, long tr, float pulso, float alpha) {
        if (alpha < 0.01f) return;
        float cx = w / 2f;
        float cy = h / 2f;
        float raio = Math.min(w, h) * 0.36f;
        float rotacao = tr * 0.00012f;
        float p = (float) tr / DURACAO_DESENHO;
        int cor = argb(alpha, misturar(0x8A0A12, 0xFF2030, pulso));
        float completo = (float) (Math.PI * 2);

        float pixel = Math.max(2, Math.round(Math.min(w, h) / 110f));
        int corEscura = argb(alpha * 0.8f, 0x4A0008);
        float topo = (float) (-Math.PI / 2);

        arcoPixelado(g, cx, cy, raio, pixel, cor, topo, completo * suave(clamp01(p / 0.35f)));
        arcoPixelado(g, cx, cy, raio - pixel, pixel, corEscura, topo, completo * suave(clamp01(p / 0.35f)));
        arcoPixelado(g, cx, cy, raio * 0.8f, pixel, cor, topo, completo * suave(clamp01((p - 0.15f) / 0.3f)));
        arcoPixelado(g, cx, cy, raio * 0.3f, pixel, cor, topo, completo * suave(clamp01((p - 0.3f) / 0.3f)));

        float[] vx = new float[5];
        float[] vy = new float[5];
        for (int k = 0; k < 5; k++) {
            double angulo = topo + k * Math.PI * 2 / 5;
            vx[k] = cx + (float) Math.cos(angulo) * raio * 0.8f;
            vy[k] = cy + (float) Math.sin(angulo) * raio * 0.8f;
        }
        int[] ordem = {0, 2, 4, 1, 3, 0};
        for (int k = 0; k < 5; k++) {
            float local = clamp01((p - 0.4f - k * 0.08f) / 0.08f);
            if (local <= 0) continue;
            float ax = vx[ordem[k]], ay = vy[ordem[k]];
            float bx = vx[ordem[k + 1]], by = vy[ordem[k + 1]];
            linhaPixelada(g, ax, ay, ax + (bx - ax) * local, ay + (by - ay) * local, pixel, cor);
            arcoPixelado(g, ax, ay, raio * 0.06f, pixel, cor, 0, completo);
        }

        g.flush();
        float escala = Math.max(1f, Math.min(2.5f, raio * 0.1f / 8f));
        for (int i = 0; i < GLIFOS_ANEL; i++) {
            if (p < 0.5f + i * 0.015f) continue;
            if (random.nextFloat() < 0.02f) anel[i] = letraAleatoria(random);
            double angulo = -rotacao + i * Math.PI * 2 / GLIFOS_ANEL;
            float px = cx + (float) Math.cos(angulo) * raio * 0.9f;
            float py = cy + (float) Math.sin(angulo) * raio * 0.9f;
            Component glifo = Component.literal(String.valueOf(anel[i])).withStyle(ESTILO_GLIFO);
            g.pose().pushPose();
            g.pose().translate(px, py, 0);
            g.pose().mulPose(Axis.ZP.rotation((float) (angulo + Math.PI / 2)));
            g.pose().scale(escala, escala, 1f);
            g.drawString(font, glifo, -font.width(glifo) / 2, -4, cor, false);
            g.pose().popPose();
        }
        g.flush();
    }
}
