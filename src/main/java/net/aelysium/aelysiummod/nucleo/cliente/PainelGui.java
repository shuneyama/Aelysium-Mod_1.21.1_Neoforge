package net.aelysium.aelysiummod.nucleo.cliente;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class PainelGui {
    private static final int PRETO = 0xFF000000;
    private static final int FUNDO = 0xFFC6C6C6;
    private static final int LUZ = 0xFFFFFFFF;
    private static final int SOMBRA = 0xFF555555;
    private static final int REBAIXADO = 0xFF8B8B8B;
    private static final int REBAIXADO_SOMBRA = 0xFF373737;

    private PainelGui() {
    }

    public static void painel(GuiGraphics g, int x, int y, int l, int a) {
        g.fill(x + 1, y, x + l - 1, y + 1, PRETO);
        g.fill(x + 1, y + a - 1, x + l - 1, y + a, PRETO);
        g.fill(x, y + 1, x + 1, y + a - 1, PRETO);
        g.fill(x + l - 1, y + 1, x + l, y + a - 1, PRETO);
        g.fill(x + 1, y + 1, x + l - 1, y + a - 1, FUNDO);
        g.fill(x + 1, y + 1, x + l - 2, y + 3, LUZ);
        g.fill(x + 1, y + 1, x + 3, y + a - 2, LUZ);
        g.fill(x + 2, y + a - 3, x + l - 1, y + a - 1, SOMBRA);
        g.fill(x + l - 3, y + 2, x + l - 1, y + a - 1, SOMBRA);
        g.fill(x + l - 3, y + 1, x + l - 1, y + 3, FUNDO);
        g.fill(x + 1, y + a - 3, x + 3, y + a - 1, FUNDO);
    }

    public static void rebaixado(GuiGraphics g, int x, int y, int l, int a) {
        g.fill(x, y, x + l, y + a, REBAIXADO);
        g.fill(x, y, x + l - 1, y + 1, REBAIXADO_SOMBRA);
        g.fill(x, y, x + 1, y + a - 1, REBAIXADO_SOMBRA);
        g.fill(x + 1, y + a - 1, x + l, y + a, LUZ);
        g.fill(x + l - 1, y + 1, x + l, y + a, LUZ);
    }

    public static void textoContorno(GuiGraphics g, Font fonte, String texto, int x, int y, int rgb) {
        int contorno = 0xFF000000;
        g.drawString(fonte, texto, x - 1, y, contorno, false);
        g.drawString(fonte, texto, x + 1, y, contorno, false);
        g.drawString(fonte, texto, x, y - 1, contorno, false);
        g.drawString(fonte, texto, x, y + 1, contorno, false);
        g.drawString(fonte, texto, x, y, 0xFF000000 | rgb, false);
    }

    public static int luminosidade(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int gr = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (int) (0.299 * r + 0.587 * gr + 0.114 * b);
    }
}
