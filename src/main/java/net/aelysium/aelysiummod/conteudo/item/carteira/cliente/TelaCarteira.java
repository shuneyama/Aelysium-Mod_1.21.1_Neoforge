package net.aelysium.aelysiummod.conteudo.item.carteira.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.item.carteira.CarteiraMenu;
import net.aelysium.aelysiummod.conteudo.item.carteira.CarteiraSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class TelaCarteira extends AbstractContainerScreen<CarteiraMenu> {

    private static final ResourceLocation TEXTURA =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/gui/carteira.png");

    private static final int MINI_SLOT_U = 234;
    private static final int MINI_SLOT_V = 1;
    private static final int MINI_SLOT_TAMANHO = 18;
    private static final int SLOT_GRANDE_TAMANHO = 19;

    public TelaCarteira(CarteiraMenu menu, Inventory playerInventory, Component titulo) {
        super(menu, playerInventory, titulo);
        this.imageWidth = 176;
        this.imageHeight = 177;
        this.inventoryLabelY = 83;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURA, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int relX = mouseX - x;
        int relY = mouseY - y;
        for (Slot slot : this.menu.slots) {
            if (!(slot instanceof CarteiraSlot) || !slot.isActive()) continue;
            boolean ehVineda = slot.getContainerSlot() == CarteiraMenu.SLOTS_AEDA - 1;
            int tamanho = ehVineda ? SLOT_GRANDE_TAMANHO : MINI_SLOT_TAMANHO;
            if (relX >= slot.x - 1 && relX < slot.x - 1 + tamanho
                    && relY >= slot.y - 1 && relY < slot.y - 1 + tamanho) {
                guiGraphics.blit(TEXTURA, x + slot.x - 1, y + slot.y - (ehVineda ? 2 : 1), tamanho, tamanho,
                        MINI_SLOT_U, MINI_SLOT_V, MINI_SLOT_TAMANHO, MINI_SLOT_TAMANHO, 256, 256);
                break;
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }
}
