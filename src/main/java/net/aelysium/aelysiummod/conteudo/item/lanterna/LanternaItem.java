package net.aelysium.aelysiummod.conteudo.item.lanterna;

import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LanternaItem extends Item {

    public LanternaItem(Properties properties) {
        super(properties);
    }

    public static boolean estaLigada(ItemStack stack) {
        return stack.getOrDefault(ModComponentesDados.LANTERNA_LIGADA, false);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            boolean ligada = !estaLigada(stack);
            stack.set(ModComponentesDados.LANTERNA_LIGADA, ligada);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 0.5f, ligada ? 1.4f : 1.0f);
            player.displayClientMessage(Component.translatable(ligada
                    ? "mensagem.aelysiummod.lanterna.ligada"
                    : "mensagem.aelysiummod.lanterna.desligada"), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
