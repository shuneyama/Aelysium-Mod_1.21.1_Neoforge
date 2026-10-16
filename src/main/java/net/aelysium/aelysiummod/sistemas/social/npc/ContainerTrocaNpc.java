package net.aelysium.aelysiummod.sistemas.social.npc;

import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;

public class ContainerTrocaNpc extends MerchantContainer {

    public static final int LIMITE_STACK = 999999999;

    public ContainerTrocaNpc(Merchant merchant) {
        super(merchant);
    }

    @Override
    public int getMaxStackSize() {
        return LIMITE_STACK;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return LIMITE_STACK;
    }
}
