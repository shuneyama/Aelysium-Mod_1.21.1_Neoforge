package net.aelysium.aelysiummod.sistemas.mundo.porta.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ChaveItem extends CartaoItem {

    public ChaveItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (estaVinculado(stack)) {
            String tipo = getTipoVinculo(stack);
            if ("cofre".equals(tipo)) {
                tooltip.add(Component.translatable("tooltip.aelysiummod.porta.vinculada_a_um_cofre"));
            } else if ("porta".equals(tipo)) {
                tooltip.add(Component.translatable("tooltip.aelysiummod.porta.vinculada_a_uma_porta"));
            } else {
                tooltip.add(Component.translatable("tooltip.aelysiummod.porta.vinculada"));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.aelysiummod.porta.esta_chave_ainda_nao_foi_vinculada"));
        }
    }
}
