package net.aelysium.aelysiummod.sistemas.social.npc.cliente;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class CampoNumeroNpc extends EditBox {
    private static final int PADDING = 4;
    private static final int COR_TEXTO = 0xFFE0E0E0;
    private static final int COR_INVISIVEL = 0x04FFFFFF;
    private final Font fonte;
    private boolean centralizado;
    private int corPainel;

    public CampoNumeroNpc(Font fonte, int x, int y, int largura, int altura) {
        super(fonte, x, y, largura, altura, Component.empty());
        this.fonte = fonte;
        setBordered(false);
        setTextColor(COR_INVISIVEL);
        setTextColorUneditable(COR_INVISIVEL);
    }

    public CampoNumeroNpc centralizarSemMoldura(int corPainel) {
        this.centralizado = true;
        this.corPainel = 0xFF000000 | corPainel;
        return this;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(codePoint == ',' ? '.' : codePoint, modifiers);
    }

    @Override
    public int getInnerWidth() {
        return 100000;
    }

    private static float pixelsReaisPorUnidade() {
        double guiEscala = Minecraft.getInstance().getWindow().getGuiScale();
        float escalaTela = Minecraft.getInstance().screen instanceof TelaEditorNpc t ? t.escala : 1f;
        return Math.max(1f, (float) (guiEscala * escalaTela));
    }

    private static float alinhar(float valor, float pixels) {
        return Math.round(valor * pixels) / pixels;
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int larguraReal = getWidth();
        if (centralizado) {
            g.fill(x, y, x + larguraReal, y + getHeight(), corPainel);
            g.fill(x + 1, y + 1, x + larguraReal - 1, y + getHeight() - 1, 0xFF000000);
        } else {
            g.fill(x + 1, y + 1, x + larguraReal - 2, y + getHeight() - 1, 0xFF000000);
        }

        String valor = getValue();
        float pixels = pixelsReaisPorUnidade();
        int larguraTexto = Math.max(1, fonte.width(valor) - 1);
        float escala = 1f;
        float inicioX = x + PADDING;
        if (centralizado) {
            float util = larguraReal - 4;
            if (larguraTexto > util) {
                float passos = Math.min((float) Math.floor(util / larguraTexto * pixels), Math.max(1f, pixels - 1f));
                escala = Math.max(1f, passos) / pixels;
            }
            inicioX = alinhar(x + (larguraReal - larguraTexto * escala) / 2f, pixels);
        } else if (larguraTexto > larguraReal - PADDING * 2) {
            float util = larguraReal - 3;
            float passos = Math.min((float) Math.floor(util / larguraTexto * pixels), Math.max(1f, pixels - 1f));
            escala = Math.max(1f, passos) / pixels;
            inicioX = alinhar(x + 1 + (util - larguraTexto * escala) / 2f, pixels);
        }
        float centroDestino = y + getHeight() / 2f + 0.5f;
        float topo = alinhar(centroDestino - 4f * escala, pixels);

        g.pose().pushPose();
        g.pose().translate(inicioX, topo, 0);
        g.pose().scale(escala, escala, 1);
        g.pose().translate(-x, -y, 0);
        g.drawString(fonte, valor, x, y, COR_TEXTO, false);
        boolean piscando = (System.currentTimeMillis() / 300) % 2 == 0;
        if (isFocused() && piscando && getCursorPosition() == valor.length() && valor.length() < 12) {
            g.drawString(fonte, "_", x + fonte.width(valor), y, COR_TEXTO, false);
        }
        setWidth(100000);
        try {
            super.renderWidget(g, mouseX, mouseY, partialTick);
        } finally {
            setWidth(larguraReal);
        }
        g.pose().popPose();
    }
}
