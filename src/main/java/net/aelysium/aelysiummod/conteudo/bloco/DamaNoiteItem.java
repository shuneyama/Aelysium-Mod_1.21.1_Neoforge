package net.aelysium.aelysiummod.conteudo.bloco;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.function.BooleanSupplier;

public class DamaNoiteItem extends BlockItem {

    public static BooleanSupplier luaVermelhaNoCliente = () -> false;

    public DamaNoiteItem(Block bloco, Properties propriedades) {
        super(bloco, propriedades);
    }

    @Override
    public Component getName(ItemStack stack) {
        if (luaVermelhaNoCliente.getAsBoolean()) return Component.translatable("block.aelysiummod.dama_vermelha_da_noite");
        return super.getName(stack);
    }
}
