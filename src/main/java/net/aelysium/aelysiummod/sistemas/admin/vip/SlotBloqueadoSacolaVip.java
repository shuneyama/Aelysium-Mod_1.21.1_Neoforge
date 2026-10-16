package net.aelysium.aelysiummod.sistemas.admin.vip;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotBloqueadoSacolaVip extends Slot {

    public SlotBloqueadoSacolaVip(Container container, int indice, int x, int y) {
        super(container, indice, x, y);
    }

    @Override
    public boolean mayPickup(Player jogador) {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
