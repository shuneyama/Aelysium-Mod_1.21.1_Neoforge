package net.aelysium.aelysiummod.conteudo.item.carteira;

import net.aelysium.aelysiummod.conteudo.item.carteira.CarteiraMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class CarteiraItem extends Item {

    public CarteiraItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }
        abrir(serverPlayer, stack);
        return InteractionResultHolder.success(stack);
    }

    public static void abrir(ServerPlayer serverPlayer, ItemStack stack) {
        serverPlayer.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, player) -> new CarteiraMenu(containerId, playerInventory, stack),
                Component.translatable("item.aelysiummod.carteira")
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.aelysiummod.carteira.guarda_suas_aedas_com_seguranca"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
