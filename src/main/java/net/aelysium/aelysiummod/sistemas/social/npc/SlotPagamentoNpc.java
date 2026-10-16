package net.aelysium.aelysiummod.sistemas.social.npc;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotPagamentoNpc extends Slot {

    public SlotPagamentoNpc(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public int getMaxStackSize() {
        return ContainerTrocaNpc.LIMITE_STACK;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return ContainerTrocaNpc.LIMITE_STACK;
    }
}
