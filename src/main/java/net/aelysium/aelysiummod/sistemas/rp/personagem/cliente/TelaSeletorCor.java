package net.aelysium.aelysiummod.sistemas.rp.personagem.cliente;

import net.aelysium.aelysiummod.nucleo.cliente.InsigniasPersonalizadasCliente;
import net.aelysium.aelysiummod.nucleo.cliente.PainelGui;
import net.aelysium.aelysiummod.nucleo.jogador.GeradorInsignia;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.IntConsumer;

public class TelaSeletorCor extends Screen {
    private static final int RODA = 96;
    private static final int BARRA = 12;
    private static final int LARGURA = 164;
    private static final int ALTURA = 192;

    private enum Arrasto { NENHUM, RODA, BRILHO }

    private final Screen pai;
    private final boolean opcional;
    private final IntConsumer aoConfirmar;
    private float matiz;
    private float saturacao;
    private float brilho;
    private int cor;
    private Arrasto arrasto = Arrasto.NENHUM;
    private ResourceLocation textura;
    private float brilhoDaTextura = -1f;
    private int x0;
    private int y0;
    private int rodaX;
    private int rodaY;
    private int barraY;
    private final String previa;

    public TelaSeletorCor(Screen pai, int corInicial, boolean opcional, String previa, IntConsumer aoConfirmar) {
        super(Component.translatable("gui.aelysiummod.personagens.cor_titulo"));
        this.pai = pai;
        this.opcional = opcional;
        this.previa = previa == null || previa.isBlank() ? Component.translatable("gui.aelysiummod.deuses.previa").getString() : previa;
        this.aoConfirmar = aoConfirmar;
        this.cor = corInicial >= 0 ? corInicial & 0xFFFFFF : 0xFFFFFF;
        float[] hsv = rgbParaHsv(cor);
        matiz = hsv[0];
        saturacao = hsv[1];
        brilho = hsv[2];
    }

    @Override
    protected void init() {
        x0 = (width - LARGURA) / 2;
        y0 = (height - ALTURA) / 2;
        rodaX = x0 + (LARGURA - RODA) / 2;
        rodaY = y0 + 22;
        barraY = rodaY + RODA + 8;
        int y = y0 + ALTURA - 28;
        int n = opcional ? 3 : 2;
        int l = (LARGURA - 20 - (n - 1) * 4) / n;
        int x = x0 + 10;
        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.personagens.cor_ok"), b -> {
            aoConfirmar.accept(cor);
            onClose();
        }).bounds(x, y, l, 20).build());
        x += l + 4;
        if (opcional) {
            addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.personagens.cor_limpar"), b -> {
                aoConfirmar.accept(-1);
                onClose();
            }).bounds(x, y, l, 20).build());
            x += l + 4;
        }
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose()).bounds(x, y, l, 20).build());
    }

    private void atualizarTextura() {
        if (textura != null && Math.abs(brilhoDaTextura - brilho) < 0.004f) return;
        brilhoDaTextura = brilho;
        InsigniasPersonalizadasCliente.liberar(textura);
        int[] pixels = new int[RODA * RODA];
        float raio = RODA / 2f;
        for (int y = 0; y < RODA; y++) {
            for (int x = 0; x < RODA; x++) {
                float dx = x + 0.5f - raio;
                float dy = y + 0.5f - raio;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d > raio) continue;
                float h = (float) (Math.atan2(dy, dx) / (Math.PI * 2.0));
                if (h < 0f) h += 1f;
                pixels[y * RODA + x] = 0xFF000000 | hsvParaRgb(h, Math.min(1f, d / raio), brilho);
            }
        }
        textura = InsigniasPersonalizadasCliente.registrar("personagem_roda", new GeradorInsignia.Imagem(RODA, RODA, pixels));
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float parcial) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float parcial) {
        pai.render(g, -1, -1, parcial);
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);
        g.fill(0, 0, width, height, 0x90000000);
        PainelGui.painel(g, x0, y0, LARGURA, ALTURA);
        atualizarTextura();
        desenharTituloCor(g);

        g.blit(textura, rodaX, rodaY, 0, 0, RODA, RODA, RODA, RODA);
        float raio = RODA / 2f;
        int mx = Math.round(rodaX + raio + (float) Math.cos(matiz * Math.PI * 2.0) * saturacao * raio);
        int my = Math.round(rodaY + raio + (float) Math.sin(matiz * Math.PI * 2.0) * saturacao * raio);
        g.renderOutline(mx - 3, my - 3, 7, 7, 0xFF000000);
        g.renderOutline(mx - 2, my - 2, 5, 5, 0xFFFFFFFF);

        g.renderOutline(rodaX - 1, barraY - 1, RODA + 2, BARRA + 2, 0xFF000000);
        for (int i = 0; i < RODA; i++) {
            g.fill(rodaX + i, barraY, rodaX + i + 1, barraY + BARRA, 0xFF000000 | hsvParaRgb(matiz, saturacao, i / (float) (RODA - 1)));
        }
        int mb = rodaX + Math.round(brilho * (RODA - 1));
        g.fill(mb - 1, barraY - 2, mb + 2, barraY + BARRA + 2, 0xFF000000);
        g.fill(mb, barraY - 1, mb + 1, barraY + BARRA + 1, 0xFFFFFFFF);

        g.drawString(font, previa, x0 + (LARGURA - font.width(previa)) / 2, barraY + BARRA + 10, 0xFF000000 | cor, true);
        super.render(g, mouseX, mouseY, parcial);
        g.pose().popPose();
    }

    private boolean naRoda(double mx, double my) {
        float raio = RODA / 2f;
        double dx = mx - (rodaX + raio);
        double dy = my - (rodaY + raio);
        return dx * dx + dy * dy <= raio * raio;
    }

    private boolean naBarra(double mx, double my) {
        return mx >= rodaX - 2 && mx < rodaX + RODA + 2 && my >= barraY - 2 && my < barraY + BARRA + 2;
    }

    private void arrastarRoda(double mx, double my) {
        float raio = RODA / 2f;
        double dx = mx - (rodaX + raio);
        double dy = my - (rodaY + raio);
        double distancia = Math.min(raio, Math.sqrt(dx * dx + dy * dy));
        double angulo = Math.atan2(dy, dx) / (Math.PI * 2.0);
        if (angulo < 0) angulo += 1.0;
        matiz = (float) angulo;
        saturacao = (float) (distancia / raio);
        cor = hsvParaRgb(matiz, saturacao, brilho);
    }

    private void arrastarBarra(double mx) {
        brilho = (float) Math.max(0.0, Math.min(1.0, (mx - rodaX) / (RODA - 1)));
        cor = hsvParaRgb(matiz, saturacao, brilho);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int botao) {
        if (botao == 0 && !editandoHex && cliqueNoHex(mx, my)) {
            abrirCampoHex();
            return true;
        }
        if (botao == 0 && editandoHex && !cliqueNoHex(mx, my)) {
            fecharCampoHex();
        }
        if (botao == 0) {
            if (naRoda(mx, my)) {
                arrasto = Arrasto.RODA;
                arrastarRoda(mx, my);
                return true;
            }
            if (naBarra(mx, my)) {
                arrasto = Arrasto.BRILHO;
                arrastarBarra(mx);
                return true;
            }
        }
        return super.mouseClicked(mx, my, botao);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int botao, double dx, double dy) {
        if (arrasto == Arrasto.RODA) {
            arrastarRoda(mx, my);
            return true;
        }
        if (arrasto == Arrasto.BRILHO) {
            arrastarBarra(mx);
            return true;
        }
        return super.mouseDragged(mx, my, botao, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int botao) {
        arrasto = Arrasto.NENHUM;
        return super.mouseReleased(mx, my, botao);
    }

    private boolean editandoHex;
    private boolean substituirHex;
    private long inicioCursor;
    private String digitosHex = "";
    private int hexX;
    private int hexY;
    private int hexL;

    private void definirCor(int rgb) {
        cor = rgb & 0xFFFFFF;
        float[] hsv = rgbParaHsv(cor);
        if (hsv[1] > 0f && hsv[2] > 0f) matiz = hsv[0];
        saturacao = hsv[1];
        brilho = hsv[2];
    }

    private void desenharTituloCor(GuiGraphics g) {
        String antes = title.getString() + " (";
        String hex = String.format("#%06X", cor & 0xFFFFFF);
        hexL = font.width("#FFFFFF") + 2;
        int x = x0 + (LARGURA - (font.width(title.getString() + " (") + font.width("#FFFFFF") + 2 + font.width(")"))) / 2;
        hexY = y0 + 8;
        hexX = x + font.width(antes);
        g.drawString(font, antes, x, hexY, 0xFF404040, false);
        String mostrado = editandoHex ? "#" + digitosHex : hex;
        g.drawString(font, mostrado, hexX, hexY, 0xFF404040, false);
        g.fill(hexX, hexY + 9, hexX + hexL, hexY + 10, 0xFF404040);
        if (editandoHex && ((System.currentTimeMillis() - inicioCursor) / 500) % 2 == 0) {
            int cx = hexX + font.width(mostrado);
            g.fill(cx, hexY - 1, cx + 1, hexY + 9, 0xFF404040);
        }
        g.drawString(font, ")", hexX + hexL, hexY, 0xFF404040, false);
    }

    private void abrirCampoHex() {
        editandoHex = true;
        substituirHex = true;
        inicioCursor = System.currentTimeMillis();
        digitosHex = String.format("%06X", cor & 0xFFFFFF);
    }

    private void fecharCampoHex() {
        editandoHex = false;
        substituirHex = false;
    }

    private boolean cliqueNoHex(double mx, double my) {
        return mx >= hexX && mx < hexX + hexL && my >= hexY - 2 && my < hexY + 11;
    }

    private void inserirHex(String texto) {
        StringBuilder limpo = new StringBuilder();
        for (char c : texto.toCharArray()) {
            if (Character.digit(c, 16) >= 0) limpo.append(Character.toUpperCase(c));
        }
        if (limpo.length() == 0) return;
        if (substituirHex) {
            digitosHex = "";
            substituirHex = false;
        }
        inicioCursor = System.currentTimeMillis();
        for (int i = 0; i < limpo.length() && digitosHex.length() < 6; i++) digitosHex += limpo.charAt(i);
        aplicarHex();
    }

    private void aplicarHex() {
        if (digitosHex.length() == 6) definirCor(Integer.parseInt(digitosHex, 16));
    }

    @Override
    public boolean charTyped(char c, int mods) {
        if (editandoHex) {
            inserirHex(String.valueOf(c));
            return true;
        }
        return super.charTyped(c, mods);
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        if (editandoHex) {
            if (tecla == 257 || tecla == 335 || tecla == 256) {
                fecharCampoHex();
                return true;
            }
            if (tecla == 259) {
                if (substituirHex) {
                    digitosHex = "";
                    substituirHex = false;
                } else if (!digitosHex.isEmpty()) {
                    inicioCursor = System.currentTimeMillis();
                    digitosHex = digitosHex.substring(0, digitosHex.length() - 1);
                }
                return true;
            }
            if (Screen.isPaste(tecla)) {
                inserirHex(minecraft.keyboardHandler.getClipboard());
                return true;
            }
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    @Override
    public void onClose() {
        InsigniasPersonalizadasCliente.liberar(textura);
        textura = null;
        minecraft.setScreen(pai);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static int hsvParaRgb(float h, float s, float v) {
        float c = v * s;
        float hh = (h - (float) Math.floor(h)) * 6f;
        float x = c * (1f - Math.abs(hh % 2f - 1f));
        float m = v - c;
        float r, g, b;
        if (hh < 1f) { r = c; g = x; b = 0; }
        else if (hh < 2f) { r = x; g = c; b = 0; }
        else if (hh < 3f) { r = 0; g = c; b = x; }
        else if (hh < 4f) { r = 0; g = x; b = c; }
        else if (hh < 5f) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        return (Math.round((r + m) * 255f) << 16) | (Math.round((g + m) * 255f) << 8) | Math.round((b + m) * 255f);
    }

    private static float[] rgbParaHsv(int rgb) {
        float r = ((rgb >> 16) & 255) / 255f;
        float g = ((rgb >> 8) & 255) / 255f;
        float b = (rgb & 255) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float d = max - min;
        float h = 0f;
        if (d != 0f) {
            if (max == r) h = ((g - b) / d) % 6f;
            else if (max == g) h = (b - r) / d + 2f;
            else h = (r - g) / d + 4f;
            h /= 6f;
            if (h < 0f) h += 1f;
        }
        return new float[]{h, max == 0f ? 0f : d / max, max};
    }
}
