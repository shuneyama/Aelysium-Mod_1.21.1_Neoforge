package net.aelysium.aelysiummod.sistemas.mundo.minifarol.cliente;

import net.aelysium.aelysiummod.sistemas.mundo.minifarol.rede.PacoteConfigurarMiniFarol;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;

public class TelaMiniFarol extends Screen {

    private static final int COLUNAS = 8;
    private static final int TAMANHO = 22;
    private static final int ESPACO = 4;

    private final BlockPos pos;
    private final int[][] posicoes = new int[16][];
    private boolean ativo;
    private int cor;

    public TelaMiniFarol(BlockPos pos, boolean ativo, int cor) {
        super(Component.translatable("gui.aelysiummod.mini_farol.titulo"));
        this.pos = pos;
        this.ativo = ativo;
        this.cor = cor & 0xFFFFFF;
    }

    @Override
    protected void init() {
        int larguraGrade = COLUNAS * TAMANHO + (COLUNAS - 1) * ESPACO;
        int inicioX = (width - larguraGrade) / 2;
        int inicioY = height / 2 - 20;

        addRenderableWidget(Button.builder(textoAtivo(), b -> {
            ativo = !ativo;
            b.setMessage(textoAtivo());
            enviar();
        }).bounds((width - 120) / 2, inicioY - 30, 120, 20).build());

        DyeColor[] cores = DyeColor.values();
        for (int i = 0; i < cores.length; i++) {
            int rgb = cores[i].getTextureDiffuseColor() & 0xFFFFFF;
            int x = inicioX + (i % COLUNAS) * (TAMANHO + ESPACO);
            int y = inicioY + (i / COLUNAS) * (TAMANHO + ESPACO);
            posicoes[i] = new int[]{x, y, rgb};
            addRenderableWidget(Button.builder(Component.empty(), b -> {
                cor = rgb;
                enviar();
            }).bounds(x, y, TAMANHO, TAMANHO)
                    .tooltip(Tooltip.create(Component.translatable("color.minecraft." + cores[i].getName())))
                    .build());
        }
    }

    private Component textoAtivo() {
        return Component.translatable(ativo ? "gui.aelysiummod.mini_farol.ligado" : "gui.aelysiummod.mini_farol.desligado");
    }

    private void enviar() {
        PacketDistributor.sendToServer(new PacoteConfigurarMiniFarol(pos, ativo, cor));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float parcial) {
        super.render(g, mouseX, mouseY, parcial);
        for (int[] p : posicoes) {
            if (p == null) continue;
            g.fill(p[0] + 4, p[1] + 4, p[0] + TAMANHO - 4, p[1] + TAMANHO - 4, 0xFF000000 | p[2]);
            if (p[2] == cor) {
                g.renderOutline(p[0] - 1, p[1] - 1, TAMANHO + 2, TAMANHO + 2, 0xFFFFFFFF);
            }
        }
        g.drawCenteredString(font, title, width / 2, height / 2 - 70, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
