package net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura.PrimitivasDesenhoMorte.*;

@OnlyIn(Dist.CLIENT)
public class DesenhoTituloRitualMorte {

    private static final int COR = 0xB3121A;
    private static final long DURACAO_ENTRADA = 250;
    private static final long DURACAO_SAIDA = 700;
    private static final long DURACAO_BRASA = 90;
    private static final int COR_SANGUE_SECO = 0x6A0008;
    private static final int COR_BULBO = 0x9A0C14;
    private static final int COR_REFLEXO = 0xE04456;

    private record GotaTitulo(float coluna, long inicio, int largura, float comprimentoMax, float velocidade, float semente) {
    }

    private final Random random;
    private final String texto;
    private final String subtitulo;
    private final long[] resolve;
    private final char[] glifos;
    private final char[] glifosSubtitulo;
    private final boolean[] gotaCriada;
    private final List<GotaTitulo> gotas = new ArrayList<>();

    private float[] slotX;
    private float[] slotLargura;
    private long glitchAte;

    public DesenhoTituloRitualMorte(Random random, String texto, String subtitulo) {
        this.random = random;
        this.texto = texto;
        this.subtitulo = subtitulo;
        this.resolve = new long[texto.length()];
        this.glifos = letrasAleatorias(random, texto.length());
        this.glifosSubtitulo = letrasAleatorias(random, Math.max(1, subtitulo.length()));
        this.gotaCriada = new boolean[texto.length()];
        for (int i = 0; i < resolve.length; i++) {
            resolve[i] = 250 + i * 40L + random.nextInt(260);
        }
    }

    public void desenhar(GuiGraphics g, Font font, int w, int h, long tt, long agora, long duracao) {
        if (slotX == null) {
            medir(font);
        }

        float alpha = envelope(tt, 0, DURACAO_ENTRADA, duracao - DURACAO_SAIDA, duracao);
        boolean glitch = tt < 400 || agora < glitchAte;
        if (!glitch && random.nextFloat() < 0.045f) {
            glitchAte = agora + 70 + random.nextInt(111);
            glitch = true;
        }
        if (glitch && random.nextFloat() < 0.25f) {
            alpha *= 0.35f;
        }

        for (int i = 0; i < glifos.length; i++) {
            if (tt < resolve[i] && random.nextFloat() < 0.35f) glifos[i] = letraAleatoria(random);
        }
        for (int i = 0; i < glifosSubtitulo.length; i++) {
            if (random.nextFloat() < 0.1f) glifosSubtitulo[i] = letraAleatoria(random);
        }

        int larguraTexto = font.width(texto);
        float escala = Math.min(3.2f, w * 0.86f / larguraTexto);
        float alturaTexto = font.lineHeight * escala;
        float cx = w / 2f;
        float y = h / 2f - alturaTexto / 2f - h * 0.04f;
        float tremorX = glitch ? (random.nextFloat() - 0.5f) * 6f : 0f;

        for (int i = 0; i < resolve.length; i++) {
            if (gotaCriada[i] || texto.charAt(i) == ' ' || tt < resolve[i] + 100) continue;
            gotaCriada[i] = true;
            int quantidade = random.nextFloat() < 0.6f ? (random.nextFloat() < 0.25f ? 2 : 1) : 0;
            boolean primeiroEsquerdo = random.nextBoolean();
            for (int k = 0; k < quantidade; k++) {
                boolean tracoEsquerdo = k == 0 ? primeiroEsquerdo : !primeiroEsquerdo;
                float coluna = slotX[i] + (tracoEsquerdo ? 0 : Math.max(0, slotLargura[i] - 2));
                gotas.add(new GotaTitulo(coluna, tt + random.nextInt(400), random.nextFloat() < 0.25f ? 2 : 1,
                        3f + random.nextFloat() * 11f, 2f + random.nextFloat() * 4f, random.nextFloat() * 100f));
            }
        }
        desenharGotas(g, cx - larguraTexto * escala / 2f, y + 6f * escala, escala, h, tt, alpha);
        g.flush();

        float borrao = escala * (1.2f + (glitch ? 2.5f : 0f));
        for (int k = 1; k <= 3; k++) {
            desenharLinha(g, font, cx + tremorX - borrao * k, y, escala, COR, alpha * 0.12f, tt, false);
            desenharLinha(g, font, cx + tremorX + borrao * k * 0.6f, y, escala, COR, alpha * 0.08f, tt, false);
        }
        if (glitch) {
            desenharLinha(g, font, cx + tremorX - 1.5f, y, escala, 0xFF2A2A, alpha * 0.55f, tt, false);
            desenharLinha(g, font, cx + tremorX + 1.5f, y, escala, 0xFFFFFF, alpha * 0.25f, tt, false);
        }
        desenharLinha(g, font, cx + tremorX, y, escala, COR, alpha, tt, true);

        int topo = (int) y;
        int base = (int) Math.ceil(y + alturaTexto);
        int esquerda = (int) (cx - larguraTexto * escala / 2f) - 4;
        int direita = (int) (cx + larguraTexto * escala / 2f) + 4;

        if (glitch && alpha >= 0.03f) {
            g.flush();
            int fatias = 2 + random.nextInt(4);
            for (int i = 0; i < fatias; i++) {
                int fy = topo + random.nextInt(Math.max(1, base - topo));
                int fh = 1 + random.nextInt(3);
                float deslocamento = (random.nextFloat() - 0.5f) * 18f;
                g.enableScissor(0, fy, w, fy + fh);
                g.fill(0, fy, w, fy + fh, 0xFF000000);
                g.flush();
                desenharLinha(g, font, cx + tremorX + deslocamento, y, escala, COR, alpha, tt, false);
                g.disableScissor();
            }
            int cortes = 1 + random.nextInt(3);
            for (int i = 0; i < cortes; i++) {
                int ly = topo + random.nextInt(Math.max(1, base - topo));
                int inicio = esquerda + random.nextInt(Math.max(1, (direita - esquerda) / 2));
                int fim = inicio + (direita - esquerda) / 3 + random.nextInt(Math.max(1, (direita - esquerda) / 2));
                g.fill(inicio, ly, Math.min(direita, fim), ly + 1, 0xFF000000);
            }
        }

        if (alpha >= 0.03f) {
            for (int ly = topo; ly < base; ly += 3) {
                g.fill(esquerda, ly, direita, ly + 1, argb(0.35f, 0x000000));
            }
        }

        desenharSubtitulo(g, font, cx, base + 8 * Math.max(1f, escala * 0.5f), escala, alpha, glitch);
    }

    private void desenharGotas(GuiGraphics g, float esquerda, float topo, float pixel, int h, long tt, float alpha) {
        if (alpha < 0.03f) return;
        for (GotaTitulo gota : gotas) {
            float s = (tt - gota.inicio()) / 1000f;
            if (s < 0) continue;
            float avanco = Math.max(0, gota.velocidade() * (s + 0.22f * (float) Math.sin(s * 4.5f + gota.semente())));
            float comprimento = Math.min(gota.comprimentoMax(), avanco);
            float excesso = Math.max(0, avanco - gota.comprimentoMax());
            int blocos = (int) comprimento;
            float x = esquerda + gota.coluna() * pixel;
            float larguraTrilha = gota.largura() * pixel;

            for (int k = 0; k < blocos; k++) {
                int cor = misturar(COR, COR_SANGUE_SECO, 0.8f * k / Math.max(1, blocos));
                retangulo(g, x, topo + k * pixel, x + larguraTrilha, topo + (k + 1) * pixel, argb(alpha, cor));
            }

            float pontaY = topo + blocos * pixel;
            if (excesso <= 0 && comprimento >= 2f) {
                float bulboX = gota.semente() > 50f ? x - pixel : x;
                float larguraBulbo = larguraTrilha + pixel;
                retangulo(g, bulboX, pontaY, bulboX + larguraBulbo, pontaY + 2 * pixel, argb(alpha, COR_BULBO));
                retangulo(g, bulboX, pontaY, bulboX + pixel, pontaY + pixel, argb(alpha * 0.8f, COR_REFLEXO));
            } else {
                retangulo(g, x, pontaY, x + larguraTrilha, pontaY + pixel, argb(alpha, COR_BULBO));
            }

            if (excesso > 0) {
                float quedaY = pontaY + pixel + excesso * excesso * 0.6f * pixel;
                if (quedaY < h) {
                    retangulo(g, x, quedaY, x + larguraTrilha, quedaY + 2 * pixel, argb(alpha, COR_BULBO));
                    retangulo(g, x, quedaY, x + pixel, quedaY + pixel, argb(alpha * 0.7f, COR_REFLEXO));
                }
            }
        }
    }

    private void medir(Font font) {
        slotX = new float[texto.length()];
        slotLargura = new float[texto.length()];
        for (int i = 0; i < texto.length(); i++) {
            slotX[i] = font.width(texto.substring(0, i));
            slotLargura[i] = font.width(String.valueOf(texto.charAt(i)));
        }
    }

    private void desenharLinha(GuiGraphics g, Font font, float x, float y, float escala, int rgb, float alpha, long tt, boolean principal) {
        if (alpha < 0.03f) return;
        float esquerda = -font.width(texto) / 2f;
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(escala, escala, 1f);
        for (int i = 0; i < texto.length(); i++) {
            char letra = texto.charAt(i);
            if (letra == ' ') continue;
            Component componente;
            float posicao;
            int cor;
            if (tt >= resolve[i]) {
                componente = Component.literal(String.valueOf(letra));
                posicao = esquerda + slotX[i];
                cor = argb(alpha, principal && tt - resolve[i] < DURACAO_BRASA ? 0xFFE0E0 : rgb);
            } else {
                componente = Component.literal(String.valueOf(glifos[i])).withStyle(ESTILO_GLIFO);
                posicao = esquerda + slotX[i] + (slotLargura[i] - font.width(componente)) / 2f;
                cor = argb(alpha * 0.7f, rgb);
            }
            float onda = (float) Math.sin(i * 0.8f + tt * 0.005f) * 0.9f;
            float esticoY = 1f + (float) Math.sin(i * 1.7f + tt * 0.003f) * 0.08f;
            g.pose().pushPose();
            g.pose().translate(posicao, onda, 0);
            g.pose().scale(1f, esticoY, 1f);
            g.drawString(font, componente, 0, 0, cor, false);
            g.pose().popPose();
        }
        g.pose().popPose();
    }

    private void desenharSubtitulo(GuiGraphics g, Font font, float cx, float y, float escalaTitulo, float alphaTitulo, boolean glitch) {
        float alpha = alphaTitulo * 0.55f * (random.nextFloat() < 0.08f ? 0.3f : 1f);
        if (alpha < 0.03f) return;
        float escala = Math.max(1f, escalaTitulo * 0.42f);
        boolean legivel = glitch && random.nextFloat() < 0.15f;
        Component componente = legivel
                ? Component.literal(subtitulo)
                : Component.literal(new String(glifosSubtitulo)).withStyle(ESTILO_GLIFO);
        g.pose().pushPose();
        g.pose().translate(cx, y, 0);
        g.pose().scale(escala, escala, 1f);
        g.drawString(font, componente, -font.width(componente) / 2, 0, argb(alpha, legivel ? 0xE8E8E8 : 0x8A0A12), false);
        g.pose().popPose();
    }
}
