package net.aelysium.aelysiummod.sistemas.rp.deus.cliente;

import net.aelysium.aelysiummod.nucleo.cliente.PainelGui;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

public class TelaPoderes extends Screen {
    private static final int COLUNA = 128;
    private static final int LARGURA = COLUNA * 2 + 20;
    private static final int LINHA = 24;
    private static final int COR_TEXTO = 0xFF404040;

    private static final int[] BITS = {
            TipoDeus.INVULNERAVEL, TipoDeus.VIDA_ALTA, TipoDeus.PARTICULA, TipoDeus.DANO_MORTAL,
            TipoDeus.VOO, TipoDeus.SEM_FOME
    };
    private static final String[] CHAVES = {
            "invulnerabilidade", "vida_alta", "particulas", "dano_mortal", "voo", "sem_fome"
    };

    private final Screen pai;
    private final IntConsumer aoAlterar;
    private int poderes;
    private int x0;
    private int y0;
    private int altura;
    private int hover = -1;

    public TelaPoderes(Screen pai, int poderes, IntConsumer aoAlterar) {
        super(Component.translatable("gui.aelysiummod.poderes.titulo"));
        this.pai = pai;
        this.poderes = poderes;
        this.aoAlterar = aoAlterar;
    }

    @Override
    protected void init() {
        int linhas = (BITS.length + 1) / 2;
        altura = 36 + linhas * LINHA + 34 + 40;
        x0 = (width - LARGURA) / 2;
        y0 = (height - altura) / 2;
        for (int i = 0; i < BITS.length; i++) {
            int bit = BITS[i];
            int coluna = i / linhas;
            int linha = i % linhas;
            Checkbox caixa = Checkbox.builder(Component.empty(), font)
                    .pos(x0 + 10 + coluna * COLUNA, y0 + 30 + linha * LINHA)
                    .selected((poderes & bit) != 0)
                    .onValueChange((c, valor) -> {
                        poderes = valor ? poderes | bit : poderes & ~bit;
                        aoAlterar.accept(poderes);
                    })
                    .build();
            addRenderableWidget(caixa);
        }
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(x0 + LARGURA / 2 - 50, y0 + altura - 28, 100, 20).build());
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
        PainelGui.painel(g, x0, y0, LARGURA, altura);
        g.drawString(font, title, x0 + (LARGURA - font.width(title)) / 2, y0 + 8, COR_TEXTO, false);
        super.render(g, mouseX, mouseY, parcial);
        int linhas = (BITS.length + 1) / 2;
        hover = -1;
        for (int i = 0; i < BITS.length; i++) {
            int bx = x0 + 10 + (i / linhas) * COLUNA;
            int by = y0 + 30 + (i % linhas) * LINHA;
            Component rotulo = Component.translatable("gui.aelysiummod.poderes." + CHAVES[i]);
            g.drawString(font, rotulo, bx + 24, by + 5, COR_TEXTO, false);
            if (mouseX >= bx && mouseX < bx + COLUNA - 4 && mouseY >= by && mouseY < by + 20) hover = i;
        }
        if (hover >= 0) {
            var linhasDica = font.split(Component.translatable("gui.aelysiummod.poderes." + CHAVES[hover] + ".dica"), LARGURA - 20);
            int ty = y0 + 30 + linhas * LINHA + 4;
            for (int i = 0; i < linhasDica.size() && i < 3; i++) g.drawString(font, linhasDica.get(i), x0 + 10, ty + i * 10, 0xFF606060, false);
        }
        g.pose().popPose();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(pai);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
