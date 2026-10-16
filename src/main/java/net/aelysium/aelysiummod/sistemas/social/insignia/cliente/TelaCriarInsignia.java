package net.aelysium.aelysiummod.sistemas.social.insignia.cliente;

import net.aelysium.aelysiummod.nucleo.cliente.InsigniasPersonalizadasCliente;
import net.aelysium.aelysiummod.nucleo.jogador.GeradorInsignia;
import net.aelysium.aelysiummod.sistemas.social.insignia.rede.RedeInsignia;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TelaCriarInsignia extends Screen {

    private static final int RODA_TAMANHO = 96;
    private static final int BARRA_LARGURA = 12;
    private static final int LINHA_ALTURA = 24;
    private static final int BOTAO_EXCLUIR = 18;

    private enum Arrasto { NENHUM, RODA, BRILHO }

    private final Screen pai;
    private EditBox campoTexto;
    private EditBox campoCor;

    private float matiz = 0f;
    private float saturacao = 1f;
    private float brilho = 0.66f;
    private int corAtual = 0xA90202;
    private boolean atualizandoCampo = false;
    private Arrasto arrasto = Arrasto.NENHUM;

    private ResourceLocation texturaRoda;
    private float brilhoDaRoda = -1f;

    private String ultimoTexto = null;
    private int ultimaCor = -1;
    private ResourceLocation texturaPrevia;
    private int larguraPrevia;

    private int rolagem = 0;
    private int esq;
    private int rodaX;
    private int rodaY;
    private int barraX;
    private int listaX;

    public TelaCriarInsignia(Screen pai) {
        super(Component.translatable("gui.aelysiummod.insignia.criar_titulo"));
        this.pai = pai;
        float[] hsv = rgbParaHsv(corAtual);
        matiz = hsv[0];
        saturacao = hsv[1];
        brilho = hsv[2];
    }

    @Override
    protected void init() {
        esq = this.width / 2 - 170;
        rodaX = esq + 100;
        rodaY = 68;
        barraX = rodaX + RODA_TAMANHO + 8;
        listaX = barraX + BARRA_LARGURA + 20;

        campoTexto = new EditBox(this.font, esq, 42, 160, 18, Component.translatable("gui.aelysiummod.insignia.texto"));
        campoTexto.setMaxLength(GeradorInsignia.MAX_CARACTERES);
        campoTexto.setFilter(t -> t.matches("[\\p{L} ]*"));
        addRenderableWidget(campoTexto);

        campoCor = new EditBox(this.font, esq, 80, 80, 18, Component.translatable("gui.aelysiummod.insignia.cor"));
        campoCor.setMaxLength(7);
        campoCor.setValue("#" + String.format("%06X", corAtual));
        campoCor.setResponder(this::aoDigitarCor);
        addRenderableWidget(campoCor);

        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.insignia.salvar"), b -> salvar())
                .bounds(esq, 232, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.insignia.voltar"), b -> onClose())
                .bounds(esq + 110, 232, 100, 20).build());
    }

    private void aoDigitarCor(String texto) {
        if (atualizandoCampo) return;
        String limpo = texto.startsWith("#") ? texto.substring(1) : texto;
        if (limpo.length() != 6) return;
        try {
            corAtual = Integer.parseInt(limpo, 16);
            float[] hsv = rgbParaHsv(corAtual);
            if (hsv[1] > 0f && hsv[2] > 0f) matiz = hsv[0];
            saturacao = hsv[1];
            brilho = hsv[2];
        } catch (NumberFormatException ignored) {
        }
    }

    private void aplicarHsv() {
        corAtual = hsvParaRgb(matiz, saturacao, brilho);
        atualizandoCampo = true;
        campoCor.setValue("#" + String.format("%06X", corAtual));
        atualizandoCampo = false;
    }

    private void salvar() {
        String texto = GeradorInsignia.normalizar(campoTexto.getValue());
        if (texto.isEmpty()) return;
        PacketDistributor.sendToServer(new RedeInsignia.PacoteCriarInsignia(texto, corAtual));
    }

    private void atualizarRoda() {
        if (texturaRoda != null && Math.abs(brilhoDaRoda - brilho) < 0.004f) return;
        brilhoDaRoda = brilho;
        InsigniasPersonalizadasCliente.liberar(texturaRoda);
        int[] pixels = new int[RODA_TAMANHO * RODA_TAMANHO];
        float raio = RODA_TAMANHO / 2f;
        for (int y = 0; y < RODA_TAMANHO; y++) {
            for (int x = 0; x < RODA_TAMANHO; x++) {
                float dx = x + 0.5f - raio;
                float dy = y + 0.5f - raio;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d > raio) continue;
                float h = (float) (Math.atan2(dy, dx) / (Math.PI * 2.0));
                if (h < 0f) h += 1f;
                pixels[y * RODA_TAMANHO + x] = 0xFF000000 | hsvParaRgb(h, Math.min(1f, d / raio), brilho);
            }
        }
        texturaRoda = InsigniasPersonalizadasCliente.registrar("insignia_roda", new GeradorInsignia.Imagem(RODA_TAMANHO, RODA_TAMANHO, pixels));
    }

    private void atualizarPrevia() {
        String texto = GeradorInsignia.normalizar(campoTexto.getValue());
        if (texto.equals(ultimoTexto) && corAtual == ultimaCor) return;
        ultimoTexto = texto;
        ultimaCor = corAtual;
        InsigniasPersonalizadasCliente.liberar(texturaPrevia);
        texturaPrevia = null;
        if (texto.isEmpty()) return;
        GeradorInsignia.Imagem imagem = GeradorInsignia.gerar(texto, corAtual);
        larguraPrevia = imagem.largura();
        texturaPrevia = InsigniasPersonalizadasCliente.registrar("insignia_previa", imagem);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        atualizarRoda();
        atualizarPrevia();

        g.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        g.drawString(this.font, Component.translatable("gui.aelysiummod.insignia.texto_rotulo"), esq, 30, 0xFFFFFF);
        g.drawString(this.font, Component.translatable("gui.aelysiummod.insignia.cor_rotulo"), esq, 68, 0xFFFFFF);

        g.blit(texturaRoda, rodaX, rodaY, 0, 0, RODA_TAMANHO, RODA_TAMANHO, RODA_TAMANHO, RODA_TAMANHO);
        float raio = RODA_TAMANHO / 2f;
        int marcaX = Math.round(rodaX + raio + (float) Math.cos(matiz * Math.PI * 2.0) * saturacao * raio);
        int marcaY = Math.round(rodaY + raio + (float) Math.sin(matiz * Math.PI * 2.0) * saturacao * raio);
        g.renderOutline(marcaX - 3, marcaY - 3, 7, 7, 0xFF000000);
        g.renderOutline(marcaX - 2, marcaY - 2, 5, 5, 0xFFFFFFFF);

        int corTopo = 0xFF000000 | hsvParaRgb(matiz, saturacao, 1f);
        g.fillGradient(barraX, rodaY, barraX + BARRA_LARGURA, rodaY + RODA_TAMANHO, corTopo, 0xFF000000);
        g.renderOutline(barraX - 1, rodaY - 1, BARRA_LARGURA + 2, RODA_TAMANHO + 2, 0xFF000000);
        int marcaBarra = rodaY + Math.round((1f - brilho) * (RODA_TAMANHO - 1));
        g.fill(barraX - 2, marcaBarra - 1, barraX + BARRA_LARGURA + 2, marcaBarra + 2, 0xFF000000);
        g.fill(barraX - 1, marcaBarra, barraX + BARRA_LARGURA + 1, marcaBarra + 1, 0xFFFFFFFF);

        g.fill(esq, 104, esq + 90, 126, 0xFF000000);
        g.fill(esq + 1, 105, esq + 89, 125, 0xFF000000 | corAtual);

        g.drawString(this.font, Component.translatable("gui.aelysiummod.insignia.previa"), esq, 172, 0xAAAAAA);
        g.fill(esq - 2, 184, esq + 212, 228, 0x66000000);
        if (texturaPrevia != null) {
            float escala = Math.min(4f, 204f / larguraPrevia);
            int largura = Math.round(larguraPrevia * escala);
            int altura = Math.round(GeradorInsignia.ALTURA * escala);
            g.pose().pushPose();
            g.pose().translate(esq + (210 - largura) / 2f, 184 + (44 - 8 * escala) / 2f - 2 * escala, 0);
            g.pose().scale(escala, escala, 1f);
            g.blit(texturaPrevia, 0, 0, 0, 0, larguraPrevia, GeradorInsignia.ALTURA, larguraPrevia, GeradorInsignia.ALTURA);
            g.pose().popPose();
        }

        g.drawString(this.font, Component.translatable("gui.aelysiummod.insignia.lista"), listaX, 30, 0xFFFFFF);
        List<InsigniasPersonalizadasCliente.Entrada> todas = List.copyOf(InsigniasPersonalizadasCliente.todas());
        int visiveis = visiveis();
        rolagem = Math.max(0, Math.min(rolagem, Math.max(0, todas.size() - visiveis)));
        for (int i = 0; i < visiveis && rolagem + i < todas.size(); i++) {
            InsigniasPersonalizadasCliente.Entrada e = todas.get(rolagem + i);
            int y = 44 + i * LINHA_ALTURA;
            int escala = e.largura() * 2 <= 130 ? 2 : 1;
            g.pose().pushPose();
            g.pose().translate(listaX, y, 0);
            g.pose().scale(escala, escala, 1f);
            g.blit(e.textura(), 0, 0, 0, 0, e.largura(), GeradorInsignia.ALTURA, e.largura(), GeradorInsignia.ALTURA);
            g.pose().popPose();

            int bx = listaX + 140;
            boolean sobre = mouseX >= bx && mouseX < bx + BOTAO_EXCLUIR && mouseY >= y && mouseY < y + BOTAO_EXCLUIR;
            net.aelysium.aelysiummod.nucleo.cliente.BotaoExcluir.desenhar(g, bx, y, BOTAO_EXCLUIR, BOTAO_EXCLUIR, sobre);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private int visiveis() {
        return Math.max(1, Math.min(9, (this.height - 60) / LINHA_ALTURA));
    }

    private boolean dentroDaRoda(double mx, double my) {
        float raio = RODA_TAMANHO / 2f;
        double dx = mx - (rodaX + raio);
        double dy = my - (rodaY + raio);
        return dx * dx + dy * dy <= raio * raio;
    }

    private boolean dentroDaBarra(double mx, double my) {
        return mx >= barraX - 2 && mx < barraX + BARRA_LARGURA + 2 && my >= rodaY - 2 && my < rodaY + RODA_TAMANHO + 2;
    }

    private void arrastarRoda(double mx, double my) {
        float raio = RODA_TAMANHO / 2f;
        double dx = mx - (rodaX + raio);
        double dy = my - (rodaY + raio);
        double distancia = Math.min(raio, Math.sqrt(dx * dx + dy * dy));
        double angulo = Math.atan2(dy, dx) / (Math.PI * 2.0);
        if (angulo < 0) angulo += 1.0;
        matiz = (float) angulo;
        saturacao = (float) (distancia / raio);
        aplicarHsv();
    }

    private void arrastarBarra(double my) {
        brilho = (float) Math.max(0.0, Math.min(1.0, 1.0 - (my - rodaY) / (RODA_TAMANHO - 1)));
        aplicarHsv();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int botao) {
        if (botao == 0) {
            if (dentroDaRoda(mx, my)) {
                arrasto = Arrasto.RODA;
                arrastarRoda(mx, my);
                return true;
            }
            if (dentroDaBarra(mx, my)) {
                arrasto = Arrasto.BRILHO;
                arrastarBarra(my);
                return true;
            }
            List<InsigniasPersonalizadasCliente.Entrada> todas = List.copyOf(InsigniasPersonalizadasCliente.todas());
            for (int i = 0; i < visiveis() && rolagem + i < todas.size(); i++) {
                int y = 44 + i * LINHA_ALTURA;
                int bx = listaX + 140;
                if (mx >= bx && mx < bx + BOTAO_EXCLUIR && my >= y && my < y + BOTAO_EXCLUIR) {
                    PacketDistributor.sendToServer(new RedeInsignia.PacoteExcluirInsignia(todas.get(rolagem + i).dados().id()));
                    return true;
                }
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
            arrastarBarra(my);
            return true;
        }
        return super.mouseDragged(mx, my, botao, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int botao) {
        arrasto = Arrasto.NENHUM;
        return super.mouseReleased(mx, my, botao);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (mx >= listaX) {
            rolagem -= (int) Math.signum(sy);
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override
    public void onClose() {
        InsigniasPersonalizadasCliente.liberar(texturaPrevia);
        InsigniasPersonalizadasCliente.liberar(texturaRoda);
        texturaPrevia = null;
        texturaRoda = null;
        this.minecraft.setScreen(pai);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
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
        int ri = Math.round((r + m) * 255f);
        int gi = Math.round((g + m) * 255f);
        int bi = Math.round((b + m) * 255f);
        return (ri << 16) | (gi << 8) | bi;
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
        float s = max == 0f ? 0f : d / max;
        return new float[]{h, s, max};
    }
}
