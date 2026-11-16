package net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura.PrimitivasDesenhoMorte.*;

@OnlyIn(Dist.CLIENT)
public class DesenhoCampoSangueMorte {

    private static final int COR_CORPO = 0x5E0006;
    private static final int COR_BRILHO = 0xB8202C;
    private static final int COR_REFLEXO = 0xE04456;

    private record Gota(float x, float y, long inicio, float largura, float comprimentoMax, float velocidade, float semente) {
    }

    private final Random random;
    private final List<Gota> gotas = new ArrayList<>();

    public DesenhoCampoSangueMorte(Random random) {
        this.random = random;
    }

    public void desenharCampo(GuiGraphics g, int w, int h, float tempo, float brilho, float opacidade) {
        if (brilho < 0.01f || opacidade < 0.01f) return;
        VertexConsumer vc = pincel(g);
        Matrix4f m = g.pose().last().pose();
        int celula = Math.max(3, Math.round(Math.min(w, h) / 80f));
        for (int y = 0; y < h; y += celula) {
            float gradiente = (float) y / h;
            for (int x = 0; x < w; x += celula) {
                float nx = x * 0.010f;
                float ny = y * 0.010f;
                float q = ruido(nx * 0.8f + tempo * 0.15f, ny * 0.8f - tempo * 0.1f);
                float v = fbm(nx + q * 2.2f, ny + q * 1.6f - tempo * 0.25f);
                float listra = 0.5f + 0.5f * (float) Math.sin(v * 14f + tempo * 0.6f);
                float valor = clamp01(listra * 0.6f + gradiente * 0.55f - 0.12f);
                int rgb = valor < 0.5f
                        ? misturar(0x14000A, 0x6A0012, valor * 2f)
                        : misturar(0x6A0012, 0xFF1430, (valor - 0.5f) * 2f);
                float tamanho = celula * (0.45f + 0.5f * valor);
                float ox = x + (celula - tamanho) / 2f;
                float oy = y + (celula - tamanho) / 2f;
                retangulo(vc, m, ox, oy, ox + tamanho, oy + tamanho, argb(opacidade, escurecer(rgb, brilho)));
            }
        }
    }

    public void desenharBorda(GuiGraphics g, int w, int h, float tempo, float progresso, float alpha) {
        if (progresso < 0.01f || alpha < 0.01f) return;
        VertexConsumer vc = pincel(g);
        Matrix4f m = g.pose().last().pose();
        int corpo = argb(alpha, COR_CORPO);
        int brilho = argb(alpha * 0.3f, COR_BRILHO);
        for (int x = 0; x < w; x += 2) {
            float altura = h * (0.008f + 0.045f * fbm(x * 0.02f, tempo * 0.15f)) * progresso;
            retangulo(vc, m, x, 0, x + 2, altura, corpo);
            retangulo(vc, m, x, altura - 1.4f, x + 2, altura - 0.6f, brilho);
        }
    }

    public void adicionarGota(float x, float y, long inicio, float largura, float comprimentoMax, float velocidade) {
        gotas.add(new Gota(x, y, inicio, largura, comprimentoMax, velocidade, random.nextFloat() * 100f));
    }

    public void gotejarDoTopo(int w, int h, long t, float chance) {
        if (random.nextFloat() >= chance) return;
        adicionarGota(random.nextFloat() * w, h * 0.015f, t, 1.5f + random.nextFloat() * 2f,
                h * (0.15f + random.nextFloat() * 0.45f), 25f + random.nextFloat() * 45f);
    }

    public void limparGotas() {
        gotas.clear();
    }

    public void desenharGotas(GuiGraphics g, long t, float alpha) {
        if (alpha < 0.01f) return;
        VertexConsumer vc = pincel(g);
        Matrix4f m = g.pose().last().pose();
        int corpo = argb(alpha, COR_CORPO);
        int brilho = argb(alpha * 0.45f, COR_BRILHO);

        for (Gota gota : gotas) {
            float s = (t - gota.inicio()) / 1000f;
            if (s < 0) continue;
            float avanco = Math.max(0, gota.velocidade() * (s + 0.22f * (float) Math.sin(s * 4.5f + gota.semente())));
            float comprimento = Math.min(gota.comprimentoMax(), avanco);
            float excesso = Math.max(0, avanco - gota.comprimentoMax());
            float inchaco = excesso > 0 ? 0.75f : 1f + 0.45f * clamp01((avanco - gota.comprimentoMax() * 0.8f) / (gota.comprimentoMax() * 0.2f));

            int passos = Math.max(2, (int) (comprimento / 2f));
            float ultimoX = gota.x() + desvio(gota, gota.y());
            float ultimoY = gota.y();
            float ultimaLargura = gota.largura();
            for (int i = 1; i <= passos; i++) {
                float f = (float) i / passos;
                float yy = gota.y() + comprimento * f;
                float xx = gota.x() + desvio(gota, yy);
                float largura = gota.largura() * (1f - 0.55f * f) * (1f + 0.15f * (float) Math.sin(yy * 0.3f + gota.semente()));
                quad(vc, m, ultimoX - ultimaLargura / 2f, ultimoY, ultimoX + ultimaLargura / 2f, ultimoY,
                        xx + largura / 2f, yy, xx - largura / 2f, yy, corpo);
                quad(vc, m, ultimoX - ultimaLargura * 0.35f, ultimoY, ultimoX - ultimaLargura * 0.1f, ultimoY,
                        xx - largura * 0.1f, yy, xx - largura * 0.35f, yy, brilho);
                ultimoX = xx;
                ultimoY = yy;
                ultimaLargura = largura;
            }

            elipse(g, gota.x() + desvio(gota, gota.y()), gota.y(), gota.largura() * 1.3f, gota.largura() * 0.7f, corpo, 12);

            float raio = gota.largura() * 0.75f * inchaco;
            float cabecaX = ultimoX;
            float cabecaY = ultimoY + raio * 0.4f;
            elipse(g, cabecaX, cabecaY, raio, raio * 1.25f, corpo, 14);
            elipse(g, cabecaX - raio * 0.35f, cabecaY - raio * 0.3f, raio * 0.25f, raio * 0.3f, argb(alpha * 0.7f, COR_REFLEXO), 8);

            if (excesso > 0) {
                float queda = excesso * excesso * 0.015f;
                float esticado = 1.2f + Math.min(1.5f, excesso * 0.01f);
                elipse(g, cabecaX, cabecaY + raio * 2f + queda, raio * 0.8f, raio * esticado, corpo, 12);
            }
        }
    }

    private static float desvio(Gota gota, float y) {
        return (float) Math.sin(y * 0.045f + gota.semente()) * gota.largura() * 0.35f
                + (float) Math.sin(y * 0.13f + gota.semente() * 2f) * gota.largura() * 0.12f;
    }
}
