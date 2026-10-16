package net.aelysium.aelysiummod.sistemas.social.npc.cliente;

import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class TelaConfirmarRemocao extends Screen {
    private final Screen parent;
    private final EntidadeNpcPersonalizado npc;

    public TelaConfirmarRemocao(Screen parent, EntidadeNpcPersonalizado npc) {
        super(Component.translatable("gui.aelysiummod.npc.confirmar"));
        this.parent = parent;
        this.npc    = npc;
    }

    @Override
    protected void init() {
        int cx = width / 2, cy = height / 2;
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                Component.translatable("gui.aelysiummod.npc.sim"), b -> {
                    PacketDistributor.sendToServer(new PacotesNpc.RemoverNpcC2S(npc.getId()));
                    Minecraft.getInstance().setScreen(null);
                }).bounds(cx - 55, cy + 10, 50, 20).build());
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                Component.translatable("gui.aelysiummod.npc.nao"),
                b -> Minecraft.getInstance().setScreen(parent)
        ).bounds(cx + 5, cy + 10, 50, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        g.fill(width / 2 - 100, height / 2 - 20, width / 2 + 100, height / 2 + 38, 0xDD000000);
        g.drawCenteredString(font, Component.translatable("gui.aelysiummod.npc.remover_este_npc"), width / 2, height / 2 - 12, 0xFFFFFF);
        super.render(g, mx, my, pt);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override public boolean isPauseScreen() { return true; }
}
