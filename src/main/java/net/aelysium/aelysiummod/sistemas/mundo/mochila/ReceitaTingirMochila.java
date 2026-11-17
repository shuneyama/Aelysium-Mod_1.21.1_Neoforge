package net.aelysium.aelysiummod.sistemas.mundo.mochila;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ReceitaTingirMochila extends CustomRecipe {

    public ReceitaTingirMochila(CraftingBookCategory categoria) {
        super(categoria);
    }

    @Override
    public boolean matches(CraftingInput entrada, Level mundo) {
        return resolver(entrada) != null;
    }

    private static ItemStack[] resolver(CraftingInput entrada) {
        ItemStack mochila = ItemStack.EMPTY;
        ItemStack tinta = ItemStack.EMPTY;
        for (int i = 0; i < entrada.size(); i++) {
            ItemStack pilha = entrada.getItem(i);
            if (pilha.isEmpty()) continue;
            if (ModMochilas.ehMochila(pilha)) {
                if (!mochila.isEmpty()) return null;
                mochila = pilha;
            } else if (pilha.getItem() instanceof DyeItem) {
                if (!tinta.isEmpty()) return null;
                tinta = pilha;
            } else {
                return null;
            }
        }
        if (mochila.isEmpty() || tinta.isEmpty()) return null;
        return new ItemStack[]{mochila, tinta};
    }

    @Override
    public ItemStack assemble(CraftingInput entrada, HolderLookup.Provider registros) {
        ItemStack[] par = resolver(entrada);
        if (par == null) return ItemStack.EMPTY;
        DyeColor cor = ((DyeItem) par[1].getItem()).getDyeColor();
        return par[0].transmuteCopy(ModMochilas.COLORIDAS.get(cor).get(), 1);
    }

    @Override
    public boolean canCraftInDimensions(int largura, int altura) {
        return largura * altura >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ReceitasMochila.TINGIR.get();
    }
}
