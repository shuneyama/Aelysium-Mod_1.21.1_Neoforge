package net.aelysium.aelysiummod.conteudo.item.carteira;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class CarteiraSlot extends Slot {

    public static final int LIMITE_STACK = 999999999;

    private final Supplier<? extends Item> itemPermitido;

    public CarteiraSlot(Container container, int index, int x, int y, Supplier<? extends Item> itemPermitido) {
        super(container, index, x, y);
        this.itemPermitido = itemPermitido;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(itemPermitido.get());
    }

    @Override
    public boolean isHighlightable() {
        return false;
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
