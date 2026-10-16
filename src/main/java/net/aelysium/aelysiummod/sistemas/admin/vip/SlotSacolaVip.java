package net.aelysium.aelysiummod.sistemas.admin.vip;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotSacolaVip extends Slot {

    public SlotSacolaVip(Container container, int indice, int x, int y) {
        super(container, indice, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
