package net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.Random;

import static net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura.PrimitivasDesenhoMorte.*;

@OnlyIn(Dist.CLIENT)
public final class DesenhoGlitchMorte {

    private static final int[] CORES_RUIDO = {0xFF1020, 0xFFFFFF, 0xFF00C8, 0xFFE020, 0x20E0FF};

    private DesenhoGlitchMorte() {
    }

    public static void desenharFaixasTela(GuiGraphics g, ResourceLocation textura, int w, int h, Random random) {
        int faixas = 3 + random.nextInt(4);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (random.nextFloat() < 0.75f) {
            RenderSystem.setShaderColor(1f, 0.15f, 0.15f, 1f);
        } else {
            RenderSystem.setShaderColor(1.4f, 1.4f, 1.4f, 1f);
        }
        for (int i = 0; i < faixas; i++) {
            int y = random.nextInt(Math.max(1, h));
            int altura = Math.min(h - y, 2 + random.nextInt(Math.max(3, h / 14)));
            if (altura <= 0) continue;
            int deslocamento = (random.nextBoolean() ? 1 : -1) * (4 + random.nextInt(Math.max(5, w / 18)));
            g.blit(textura, deslocamento, y, w, altura, 0, y, w, altura, w, h);
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();

        desenharRuido(g, w, h, random, 0.8f);
    }

    public static void desenharColagem(GuiGraphics g, Font font, int w, int h, long semente) {
        Random r = new Random(semente);
        VertexConsumer vc = pincel(g);
        Matrix4f m = g.pose().last().pose();

        retangulo(vc, m, 0, 0, w, h, r.nextBoolean() ? 0xFF000000 : 0xFF9A0010);

        int blocos = 7 + r.nextInt(6);
        for (int i = 0; i < blocos; i++) {
            float bw = w * (0.08f + r.nextFloat() * 0.37f);
            float bh = h * (0.1f + r.nextFloat() * 0.5f);
            float x = r.nextFloat() * w - bw * 0.3f;
            float y = r.nextFloat() * h - bh * 0.3f;
            float sorteio = r.nextFloat();
            int cor = sorteio < 0.5f ? 0xFF000000 : sorteio < 0.8f ? 0xFFEE2210 : sorteio < 0.9f ? 0xFFF0F0F0 : 0xFF4A0008;
            retangulo(vc, m, x, y, x + bw, y + bh, cor);
        }

        int grupos = 2 + r.nextInt(3);
        for (int i = 0; i < grupos; i++) {
            float x0 = r.nextFloat() * w;
            float largura = 20 + r.nextFloat() * 40;
            float y0 = r.nextFloat() * h * 0.3f;
            float y1 = y0 + h * (0.4f + r.nextFloat() * 0.6f);
            for (float x = x0; x < x0 + largura; x += 1 + r.nextInt(3)) {
                float sorteio = r.nextFloat();
                int cor = sorteio < 0.45f ? 0xFFF4F4F4 : sorteio < 0.8f ? 0xFF000000 : 0xFFEE2210;
                retangulo(vc, m, x, y0, x + 1, y1, cor);
            }
        }

        int manchas = 3 + r.nextInt(3);
        for (int i = 0; i < manchas; i++) {
            float x0 = r.nextFloat() * w;
            float y0 = r.nextFloat() * h;
            int colunas = 10 + r.nextInt(25);
            int linhas = 8 + r.nextInt(17);
            int cor = r.nextBoolean() ? 0xFFEE2210 : 0xFFF0F0F0;
            for (int yy = 0; yy < linhas; yy++) {
                for (int xx = 0; xx < colunas; xx++) {
                    if ((xx + yy) % 2 != 0 || r.nextFloat() < 0.2f) continue;
                    float px = x0 + xx * 2;
                    float py = y0 + yy * 2;
                    retangulo(vc, m, px, py, px + 1, py + 1, cor);
                }
            }
        }

        for (int i = 0; i < 160; i++) {
            float x = r.nextFloat() * w;
            float y = r.nextFloat() * h;
            float tam = 1 + r.nextInt(4);
            int cor = r.nextFloat() < 0.7f ? CORES_RUIDO[r.nextInt(2)] : CORES_RUIDO[r.nextInt(CORES_RUIDO.length)];
            retangulo(vc, m, x, y, x + tam, y + tam * (1 + r.nextInt(3)), 0xFF000000 | cor);
        }

        g.flush();
        for (int i = 0; i < 2; i++) {
            Component glifos = Component.literal(new String(letrasAleatorias(r, 8 + r.nextInt(14)))).withStyle(ESTILO_GLIFO);
            float escala = 1f + r.nextInt(3);
            g.pose().pushPose();
            g.pose().translate(r.nextFloat() * w * 0.7f, r.nextFloat() * h, 0);
            g.pose().scale(escala, escala, 1f);
            g.drawString(font, glifos, 0, 0, r.nextBoolean() ? 0xFFFF2A2A : 0xFFF0F0F0, false);
            g.pose().popPose();
        }
        g.flush();
    }

    public static void desenharRuido(GuiGraphics g, int w, int h, Random random, float intensidade) {
        if (intensidade < 0.01f) return;
        VertexConsumer vc = pincel(g);
        Matrix4f m = g.pose().last().pose();
        int faixas = 3 + random.nextInt(4);
        for (int i = 0; i < faixas; i++) {
            float y = random.nextFloat() * h;
            float altura = 1 + random.nextInt(Math.max(2, h / 30));
            float x = random.nextFloat() * w * 0.5f;
            float largura = w * (0.2f + random.nextFloat() * 0.8f);
            float sorteio = random.nextFloat();
            int rgb = sorteio < 0.5f ? 0x000000 : sorteio < 0.85f ? 0xD00C18 : 0xF0F0F0;
            retangulo(vc, m, x, y, x + largura, y + altura, argb(intensidade * (0.5f + random.nextFloat() * 0.5f), rgb));
        }
        int linhas = 2 + random.nextInt(4);
        for (int i = 0; i < linhas; i++) {
            float y = random.nextFloat() * h;
            retangulo(vc, m, 0, y, w, y + 1, argb(intensidade * 0.7f, random.nextBoolean() ? 0xFFFFFF : 0x000000));
        }
    }

}
