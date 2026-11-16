package net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura.PrimitivasDesenhoMorte.*;

@OnlyIn(Dist.CLIENT)
public class DesenhoOlhosMorte {

    private static final int COR_RABISCO = 0xC0141C;
    private static final int TOTAL_OLHOS = 16;

    private record OlhoRabiscado(float rx, float ry, float tamanho, float angulo, long abre, long fasePiscada, int semente) {
    }

    private final List<OlhoRabiscado> olhos = new ArrayList<>();

    public DesenhoOlhosMorte(Random random) {
        List<Long> aberturas = new ArrayList<>();
        for (int i = 0; i < TOTAL_OLHOS; i++) {
            aberturas.add(200L + i * 2400L / TOTAL_OLHOS + random.nextInt(120));
        }
        List<OlhoRabiscado> gerados = new ArrayList<>();
        Collections.shuffle(aberturas, random);
        for (int i = 0; i < TOTAL_OLHOS; i++) {
            double angulo = Math.PI * 2 * i / TOTAL_OLHOS + (random.nextFloat() - 0.5f) * 0.3f;
            float raio = 0.78f + random.nextFloat() * 0.2f;
            gerados.add(new OlhoRabiscado(
                    0.5f + (float) Math.cos(angulo) * 0.5f * raio,
                    0.5f + (float) Math.sin(angulo) * 0.5f * raio,
                    0.035f + random.nextFloat() * 0.065f,
                    (random.nextFloat() - 0.5f) * 1.2f,
                    aberturas.get(i),
                    random.nextInt(2600),
                    random.nextInt()));
        }
        gerados.sort((a, b) -> Long.compare(a.abre(), b.abre()));
        olhos.addAll(gerados);
    }

    public int olhosAbertos(long tf) {
        int abertos = 0;
        for (OlhoRabiscado olho : olhos) {
            if (olho.abre() <= tf) abertos++;
        }
        return abertos;
    }

    public void desenharOlhosRabiscados(GuiGraphics g, int w, int h, long tf, float alpha, float focoX, float focoY, boolean encarando) {
        if (alpha < 0.01f) return;
        long passo = tf / 83;
        for (OlhoRabiscado olho : olhos) {
            if (tf < olho.abre()) continue;
            float abertura = saidaCubica((tf - olho.abre()) / 260f);
            if (!encarando && (tf + olho.fasePiscada()) % 2600 < 140) {
                abertura *= 0.1f;
            }
            float tamanho = Math.min(w, h) * olho.tamanho() * 1.6f;
            desenharOlhoRabiscado(g, olho.rx() * w, olho.ry() * h, tamanho, olho.angulo(), abertura,
                    focoX, focoY, olho.semente() + passo, alpha, encarando);
        }
    }

    public int olhosFechados(long tf, long duracaoFechamento) {
        int fechados = 0;
        for (OlhoRabiscado olho : olhos) {
            if (momentoFechar(olho, duracaoFechamento) <= tf) fechados++;
        }
        return fechados;
    }

    public void desenharOlhosFechando(GuiGraphics g, int w, int h, long tf, long duracaoFechamento, float alpha, float focoX, float focoY) {
        if (alpha < 0.01f) return;
        long passo = tf / 83;
        for (OlhoRabiscado olho : olhos) {
            float abertura = 1f - saidaCubica((tf - momentoFechar(olho, duracaoFechamento)) / 260f);
            if (abertura <= 0.02f) continue;
            float tamanho = Math.min(w, h) * olho.tamanho() * 1.6f;
            desenharOlhoRabiscado(g, olho.rx() * w, olho.ry() * h, tamanho, olho.angulo(), abertura,
                    focoX, focoY, olho.semente() + passo, alpha, true);
        }
    }

    private static long momentoFechar(OlhoRabiscado olho, long duracaoFechamento) {
        return olho.abre() * duracaoFechamento / 2800L;
    }

    public static void desenharOlhoRabiscado(GuiGraphics g, float x, float y, float tamanho, float angulo, float abertura,
                                             float focoX, float focoY, long semente, float alpha, boolean encarando) {
        Random r = new Random(semente);
        float a = tamanho;
        float b = tamanho * 0.5f * abertura;
        float inclinacao = tamanho * 0.25f;
        float cos = (float) Math.cos(angulo);
        float sin = (float) Math.sin(angulo);
        float[] lx = {-a, inclinacao, a, -inclinacao};
        float[] ly = {0, -b, 0, b};
        float[] px = new float[4];
        float[] py = new float[4];
        for (int i = 0; i < 4; i++) {
            px[i] = x + lx[i] * cos - ly[i] * sin;
            py[i] = y + lx[i] * sin + ly[i] * cos;
        }

        if (b > 0.5f) {
            quad(g, px[0], py[0], px[1], py[1], px[2], py[2], px[3], py[3], argb(alpha * 0.9f, 0x000000));
        }

        int cor = argb(alpha, COR_RABISCO);
        for (int passada = 0; passada < 2; passada++) {
            float espessura = passada == 0 ? 1.6f : 1f;
            for (int i = 0; i < 4; i++) {
                int j = (i + 1) % 4;
                float extensao = 0.06f + r.nextFloat() * 0.08f;
                float dx = px[j] - px[i];
                float dy = py[j] - py[i];
                linha(g,
                        px[i] - dx * extensao + (r.nextFloat() - 0.5f) * 2.4f,
                        py[i] - dy * extensao + (r.nextFloat() - 0.5f) * 2.4f,
                        px[j] + dx * extensao + (r.nextFloat() - 0.5f) * 2.4f,
                        py[j] + dy * extensao + (r.nextFloat() - 0.5f) * 2.4f,
                        espessura, cor);
            }
        }

        if (abertura > 0.25f) {
            float dx = focoX - x;
            float dy = focoY - y;
            float distancia = (float) Math.sqrt(dx * dx + dy * dy);
            if (distancia > 0.001f) {
                dx /= distancia;
                dy /= distancia;
            }
            float cx = x + dx * a * 0.22f;
            float cy = y + dy * b * 0.3f;
            float anguloPupila = encarando ? (float) (Math.PI / 2) : angulo + 1.2f;
            float comprimento = b * (encarando ? 0.75f : 0.55f);
            float pdx = (float) Math.cos(anguloPupila) * comprimento;
            float pdy = (float) Math.sin(anguloPupila) * comprimento;
            linha(g, cx - pdx, cy - pdy, cx + pdx, cy + pdy, encarando ? 3f : 2f,
                    argb(alpha, encarando ? 0xFF2030 : COR_RABISCO));
        }
    }
}
