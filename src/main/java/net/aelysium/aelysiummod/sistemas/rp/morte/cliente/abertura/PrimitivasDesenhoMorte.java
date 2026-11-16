package net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public final class PrimitivasDesenhoMorte {

    public static final Style ESTILO_GLIFO = Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt"));
    public static final Style ESTILO_GLIFO_ILLAGER = Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt"));

    private PrimitivasDesenhoMorte() {
    }

    public static int argb(float alpha, int rgb) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255)));
        return a << 24 | (rgb & 0xFFFFFF);
    }

    public static float clamp01(float x) {
        return Math.max(0, Math.min(1, x));
    }

    public static float suave(float x) {
        x = clamp01(x);
        return x * x * (3f - 2f * x);
    }

    public static float saidaCubica(float x) {
        x = clamp01(x);
        float inv = 1f - x;
        return 1f - inv * inv * inv;
    }

    public static float progresso(long t, long inicio, long fim) {
        return clamp01((float) (t - inicio) / (fim - inicio));
    }

    public static float envelope(long t, long inicioEntrada, long fimEntrada, long inicioSaida, long fimSaida) {
        if (t < inicioEntrada) return 0;
        if (t < fimEntrada) return (float) (t - inicioEntrada) / (fimEntrada - inicioEntrada);
        if (t < inicioSaida) return 1;
        if (t < fimSaida) return 1 - (float) (t - inicioSaida) / (fimSaida - inicioSaida);
        return 0;
    }

    public static int misturar(int a, int b, float f) {
        f = clamp01(f);
        int r = (int) (((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * f);
        int g = (int) (((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * f);
        int bl = (int) ((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * f);
        return r << 16 | g << 8 | bl;
    }

    public static int escurecer(int rgb, float f) {
        return misturar(0x000000, rgb, f);
    }

    public static char letraAleatoria(Random random) {
        return (char) ('a' + random.nextInt(26));
    }

    public static char[] letrasAleatorias(Random random, int quantidade) {
        char[] letras = new char[quantidade];
        for (int i = 0; i < quantidade; i++) {
            letras[i] = letraAleatoria(random);
        }
        return letras;
    }

    public static VertexConsumer pincel(GuiGraphics g) {
        return g.bufferSource().getBuffer(RenderType.gui());
    }

    public static void quad(VertexConsumer vc, Matrix4f m, float x1, float y1, float x2, float y2,
                            float x3, float y3, float x4, float y4, int cor) {
        float area = (x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1);
        if (area > 0) {
            vc.addVertex(m, x1, y1, 0).setColor(cor);
            vc.addVertex(m, x4, y4, 0).setColor(cor);
            vc.addVertex(m, x3, y3, 0).setColor(cor);
            vc.addVertex(m, x2, y2, 0).setColor(cor);
        } else {
            vc.addVertex(m, x1, y1, 0).setColor(cor);
            vc.addVertex(m, x2, y2, 0).setColor(cor);
            vc.addVertex(m, x3, y3, 0).setColor(cor);
            vc.addVertex(m, x4, y4, 0).setColor(cor);
        }
    }

    public static void retangulo(VertexConsumer vc, Matrix4f m, float x1, float y1, float x2, float y2, int cor) {
        vc.addVertex(m, x1, y1, 0).setColor(cor);
        vc.addVertex(m, x1, y2, 0).setColor(cor);
        vc.addVertex(m, x2, y2, 0).setColor(cor);
        vc.addVertex(m, x2, y1, 0).setColor(cor);
    }

    public static void quad(GuiGraphics g, float x1, float y1, float x2, float y2,
                            float x3, float y3, float x4, float y4, int cor) {
        quad(pincel(g), g.pose().last().pose(), x1, y1, x2, y2, x3, y3, x4, y4, cor);
    }

    public static void retangulo(GuiGraphics g, float x1, float y1, float x2, float y2, int cor) {
        retangulo(pincel(g), g.pose().last().pose(), x1, y1, x2, y2, cor);
    }

    public static void triangulo(GuiGraphics g, float ax, float ay, float bx, float by, float cx, float cy, int cor) {
        quad(g, ax, ay, bx, by, cx, cy, cx, cy, cor);
    }

    public static void linha(GuiGraphics g, float x1, float y1, float x2, float y2, float espessura, int cor) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float comprimento = (float) Math.sqrt(dx * dx + dy * dy);
        if (comprimento < 0.001f) return;
        float nx = -dy / comprimento * espessura / 2f;
        float ny = dx / comprimento * espessura / 2f;
        quad(g, x1 + nx, y1 + ny, x1 - nx, y1 - ny, x2 - nx, y2 - ny, x2 + nx, y2 + ny, cor);
    }

    public static void elipse(GuiGraphics g, float cx, float cy, float rx, float ry, int cor, int segmentos) {
        if (rx <= 0 || ry <= 0) return;
        VertexConsumer vc = pincel(g);
        Matrix4f m = g.pose().last().pose();
        for (int i = 0; i < segmentos; i++) {
            double a0 = Math.PI * 2 * i / segmentos;
            double a1 = Math.PI * 2 * (i + 1) / segmentos;
            float x0 = cx + (float) Math.cos(a0) * rx;
            float y0 = cy + (float) Math.sin(a0) * ry;
            float x1 = cx + (float) Math.cos(a1) * rx;
            float y1 = cy + (float) Math.sin(a1) * ry;
            quad(vc, m, cx, cy, x0, y0, x1, y1, x1, y1, cor);
        }
    }

    public static void arco(GuiGraphics g, float cx, float cy, float raio, float espessura, int cor,
                            int segmentos, float inicio, float extensao) {
        if (extensao <= 0 || raio <= 0) return;
        VertexConsumer vc = pincel(g);
        Matrix4f m = g.pose().last().pose();
        float interno = raio - espessura / 2f;
        float externo = raio + espessura / 2f;
        int partes = Math.max(1, (int) Math.ceil(segmentos * extensao / (Math.PI * 2)));
        for (int i = 0; i < partes; i++) {
            double a0 = inicio + extensao * i / partes;
            double a1 = inicio + extensao * (i + 1) / partes;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            quad(vc, m, cx + c0 * interno, cy + s0 * interno, cx + c0 * externo, cy + s0 * externo,
                    cx + c1 * externo, cy + s1 * externo, cx + c1 * interno, cy + s1 * interno, cor);
        }
    }

    public static void linhaPixelada(GuiGraphics g, float x1, float y1, float x2, float y2, float pixel, int cor) {
        VertexConsumer vc = pincel(g);
        Matrix4f m = g.pose().last().pose();
        int gx = (int) Math.floor(x1 / pixel);
        int gy = (int) Math.floor(y1 / pixel);
        int fx = (int) Math.floor(x2 / pixel);
        int fy = (int) Math.floor(y2 / pixel);
        int dx = Math.abs(fx - gx);
        int dy = -Math.abs(fy - gy);
        int sx = gx < fx ? 1 : -1;
        int sy = gy < fy ? 1 : -1;
        int erro = dx + dy;
        while (true) {
            retangulo(vc, m, gx * pixel, gy * pixel, (gx + 1) * pixel, (gy + 1) * pixel, cor);
            if (gx == fx && gy == fy) break;
            int dobro = 2 * erro;
            if (dobro >= dy) {
                erro += dy;
                gx += sx;
            }
            if (dobro <= dx) {
                erro += dx;
                gy += sy;
            }
        }
    }

    public static void arcoPixelado(GuiGraphics g, float cx, float cy, float raio, float pixel, int cor, float inicio, float extensao) {
        if (extensao <= 0 || raio <= 0) return;
        VertexConsumer vc = pincel(g);
        Matrix4f m = g.pose().last().pose();
        int passos = Math.max(8, (int) (extensao * raio / pixel * 1.5f));
        int ultimoX = Integer.MIN_VALUE;
        int ultimoY = Integer.MIN_VALUE;
        for (int i = 0; i <= passos; i++) {
            double angulo = inicio + extensao * i / passos;
            int gx = (int) Math.floor((cx + Math.cos(angulo) * raio) / pixel);
            int gy = (int) Math.floor((cy + Math.sin(angulo) * raio) / pixel);
            if (gx == ultimoX && gy == ultimoY) continue;
            ultimoX = gx;
            ultimoY = gy;
            retangulo(vc, m, gx * pixel, gy * pixel, (gx + 1) * pixel, (gy + 1) * pixel, cor);
        }
    }

    public static void vinheta(GuiGraphics g, int w, int h, float intensidade, int rgb) {
        if (intensidade < 0.01f) return;
        VertexConsumer vc = pincel(g);
        Matrix4f m = g.pose().last().pose();
        int passos = 12;
        float faixaMax = Math.min(w, h) * (0.12f + 0.28f * intensidade);
        for (int i = 0; i < passos; i++) {
            int cor = argb(intensidade * (1f - (float) i / passos) * 0.2f, rgb);
            float faixa = Math.max(1, faixaMax * (i + 1) / passos);
            retangulo(vc, m, 0, 0, w, faixa, cor);
            retangulo(vc, m, 0, h - faixa, w, h, cor);
            retangulo(vc, m, 0, faixa, faixa, h - faixa, cor);
            retangulo(vc, m, w - faixa, faixa, w, h - faixa, cor);
        }
    }

    public static void linhasVarredura(GuiGraphics g, int w, int h, float alpha) {
        if (alpha < 0.01f) return;
        VertexConsumer vc = pincel(g);
        Matrix4f m = g.pose().last().pose();
        int cor = argb(alpha, 0x000000);
        for (int y = 0; y < h; y += 3) {
            retangulo(vc, m, 0, y, w, y + 1, cor);
        }
    }

    public static void granulacao(GuiGraphics g, int w, int h, Random random, int quantidade, float alpha) {
        if (alpha < 0.01f) return;
        VertexConsumer vc = pincel(g);
        Matrix4f m = g.pose().last().pose();
        for (int i = 0; i < quantidade; i++) {
            float x = random.nextInt(Math.max(1, w));
            float y = random.nextInt(Math.max(1, h));
            int cinza = 0x30 + random.nextInt(0x50);
            retangulo(vc, m, x, y, x + 1, y + 1, argb(alpha * (0.4f + random.nextFloat() * 0.6f), cinza << 16 | cinza << 8 | cinza));
        }
    }

    public static float ruido(float x, float y) {
        int xi = (int) Math.floor(x);
        int yi = (int) Math.floor(y);
        float xf = x - xi;
        float yf = y - yi;
        float a = hash(xi, yi);
        float b = hash(xi + 1, yi);
        float c = hash(xi, yi + 1);
        float d = hash(xi + 1, yi + 1);
        float u = xf * xf * (3f - 2f * xf);
        float v = yf * yf * (3f - 2f * yf);
        float topo = a + (b - a) * u;
        float base = c + (d - c) * u;
        return topo + (base - topo) * v;
    }

    public static float fbm(float x, float y) {
        return 0.55f * ruido(x, y) + 0.3f * ruido(x * 2f + 17.3f, y * 2f + 9.1f) + 0.15f * ruido(x * 4f + 3.7f, y * 4f + 31.9f);
    }

    private static float hash(int x, int y) {
        int h = x * 374761393 + y * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        h ^= h >>> 16;
        return (h & 0xFFFFFF) / (float) 0xFFFFFF;
    }
}
