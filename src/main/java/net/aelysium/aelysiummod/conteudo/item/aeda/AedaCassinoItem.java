package net.aelysium.aelysiummod.conteudo.item.aeda;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class AedaCassinoItem extends Item {

    public AedaCassinoItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.aelysiummod.aeda_cassino"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
