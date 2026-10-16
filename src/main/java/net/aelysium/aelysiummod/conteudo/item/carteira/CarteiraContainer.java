package net.aelysium.aelysiummod.conteudo.item.carteira;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class CarteiraContainer extends SimpleContainer {

    public CarteiraContainer(int size) {
        super(size);
    }

    @Override
    public int getMaxStackSize() {
        return CarteiraSlot.LIMITE_STACK;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return CarteiraSlot.LIMITE_STACK;
    }
}
