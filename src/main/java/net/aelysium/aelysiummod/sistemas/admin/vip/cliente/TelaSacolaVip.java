package net.aelysium.aelysiummod.sistemas.admin.vip.cliente;

import net.aelysium.aelysiummod.sistemas.admin.vip.MenuSacolaVip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TelaSacolaVip extends AbstractContainerScreen<MenuSacolaVip> {

    private static final ResourceLocation TEXTURA = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    public TelaSacolaVip(MenuSacolaVip menu, Inventory inventarioJogador, Component titulo) {
        super(menu, inventarioJogador, titulo);
        this.imageHeight = 114 + MenuSacolaVip.LINHAS * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graficos, int mouseX, int mouseY, float parcial) {
        super.render(graficos, mouseX, mouseY, parcial);
        this.renderTooltip(graficos, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graficos, float parcial, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graficos.blit(TEXTURA, x, y, 0, 0, this.imageWidth, MenuSacolaVip.LINHAS * 18 + 17);
        graficos.blit(TEXTURA, x, y + MenuSacolaVip.LINHAS * 18 + 17, 0, 126, this.imageWidth, 96);
    }
}
