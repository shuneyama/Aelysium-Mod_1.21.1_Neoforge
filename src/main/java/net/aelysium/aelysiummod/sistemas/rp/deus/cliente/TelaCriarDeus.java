package net.aelysium.aelysiummod.sistemas.rp.deus.cliente;

import net.aelysium.aelysiummod.nucleo.cliente.PainelGui;
import net.aelysium.aelysiummod.sistemas.rp.deus.RegistroDeuses;
import net.aelysium.aelysiummod.sistemas.rp.deus.rede.PacoteCriarDeus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

public class TelaCriarDeus extends Screen {
    private static final int LARGURA = 196;
    private static final int ALTURA = 178;
    private static final int COLUNAS = 8;
    private static final int AMOSTRA_L = 18;
    private static final int AMOSTRA_A = 12;
    private static final int AMOSTRA_ESP = 4;
    private static final int[] PALETA = {
            0xFF5555, 0xFF8C00, 0xFFD700, 0xF5F56B, 0x32CD32, 0x55FF99, 0x00CED1, 0x87CEEB,
            0x3C78FF, 0x6A5ACD, 0x8B008B, 0xD65CFF, 0xFF69B4, 0xFF9AA2, 0xC0392B, 0x8B4513,
            0xF0F0F0, 0xC8C8C8, 0x909090, 0x606060, 0x303030, 0x1A1A1A, 0x006994, 0x7FDBFF
    };

    private int x0;
    private int y0;
    private int cor = 0x7FDBFF;
    private EditBox campoNome;
    private EditBox campoCor;
    private Button botaoCriar;
    private boolean atualizando;

    public TelaCriarDeus() {
        super(Component.translatable("gui.aelysiummod.deuses.criar_titulo"));
    }

    @Override
    protected void init() {
        x0 = (width - LARGURA) / 2;
        y0 = (height - ALTURA) / 2;
        campoNome = new EditBox(font, x0 + 10, y0 + 32, LARGURA - 20, 18, Component.translatable("gui.aelysiummod.deuses.nome"));
        campoNome.setMaxLength(RegistroDeuses.TAMANHO_MAXIMO_NOME);
        campoNome.setResponder(texto -> validar());
        addRenderableWidget(campoNome);
        campoCor = new EditBox(font, x0 + 10, y0 + 68, 64, 18, Component.translatable("gui.aelysiummod.deuses.cor"));
        campoCor.setMaxLength(7);
        campoCor.setFilter(texto -> texto.matches("#?[0-9a-fA-F]{0,6}"));
        campoCor.setValue(formatarHex(cor));
        campoCor.setResponder(this::aoDigitarCor);
        addRenderableWidget(campoCor);
        int larguraBotao = (LARGURA - 20 - 4) / 2;
        int y = y0 + ALTURA - 30;
        botaoCriar = addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.deuses.confirmar"), b -> criar())
                .bounds(x0 + 10, y, larguraBotao, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.deuses.cancelar"), b -> onClose())
                .bounds(x0 + 10 + larguraBotao + 4, y, larguraBotao, 20).build());
        setInitialFocus(campoNome);
        validar();
    }

    private void aoDigitarCor(String texto) {
        if (atualizando) return;
        String limpo = texto.startsWith("#") ? texto.substring(1) : texto;
        if (limpo.length() == 6) {
            cor = Integer.parseInt(limpo, 16);
        }
        validar();
    }

    private void validar() {
        if (botaoCriar == null) return;
        String limpo = campoCor.getValue().startsWith("#") ? campoCor.getValue().substring(1) : campoCor.getValue();
        botaoCriar.active = !campoNome.getValue().trim().isEmpty()
                && !RegistroDeuses.gerarId(campoNome.getValue()).isEmpty()
                && limpo.length() == 6;
    }

    private void criar() {
        PacketDistributor.sendToServer(new PacoteCriarDeus(campoNome.getValue().trim(), cor));
        minecraft.setScreen(new TelaDeuses());
    }

    private static String formatarHex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    private int amostraX(int i) {
        return x0 + 10 + (i % COLUNAS) * (AMOSTRA_L + AMOSTRA_ESP);
    }

    private int amostraY(int i) {
        return y0 + 96 + (i / COLUNAS) * (AMOSTRA_A + AMOSTRA_ESP);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float parcial) {
        super.renderBackground(g, mouseX, mouseY, parcial);
        PainelGui.painel(g, x0, y0, LARGURA, ALTURA);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float parcial) {
        super.render(g, mouseX, mouseY, parcial);
        g.drawString(font, title, x0 + (LARGURA - font.width(title)) / 2, y0 + 8, 0xFF404040, false);
        g.drawString(font, Component.translatable("gui.aelysiummod.deuses.nome"), x0 + 10, y0 + 22, 0xFF404040, false);
        g.drawString(font, Component.translatable("gui.aelysiummod.deuses.cor"), x0 + 10, y0 + 58, 0xFF404040, false);
        g.fill(x0 + 80, y0 + 68, x0 + 98, y0 + 86, 0xFF000000);
        g.fill(x0 + 81, y0 + 69, x0 + 97, y0 + 85, 0xFF000000 | cor);
        String nome = campoNome.getValue().trim();
        String previa = nome.isEmpty() ? Component.translatable("gui.aelysiummod.deuses.previa").getString() : nome;
        PainelGui.textoContorno(g, font, previa, x0 + 104, y0 + 73, cor);
        for (int i = 0; i < PALETA.length; i++) {
            int ax = amostraX(i);
            int ay = amostraY(i);
            boolean escolhida = (PALETA[i] & 0xFFFFFF) == (cor & 0xFFFFFF);
            boolean sobre = mouseX >= ax && mouseX < ax + AMOSTRA_L && mouseY >= ay && mouseY < ay + AMOSTRA_A;
            g.fill(ax - 1, ay - 1, ax + AMOSTRA_L + 1, ay + AMOSTRA_A + 1, escolhida ? 0xFFFFFFFF : (sobre ? 0xFFC6C6C6 : 0xFF000000));
            g.fill(ax, ay, ax + AMOSTRA_L, ay + AMOSTRA_A, 0xFF000000 | PALETA[i]);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int botao) {
        if (botao == 0) {
            for (int i = 0; i < PALETA.length; i++) {
                int ax = amostraX(i);
                int ay = amostraY(i);
                if (mouseX >= ax && mouseX < ax + AMOSTRA_L && mouseY >= ay && mouseY < ay + AMOSTRA_A) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    cor = PALETA[i];
                    atualizando = true;
                    campoCor.setValue(formatarHex(cor));
                    atualizando = false;
                    validar();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, botao);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(new TelaDeuses());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
