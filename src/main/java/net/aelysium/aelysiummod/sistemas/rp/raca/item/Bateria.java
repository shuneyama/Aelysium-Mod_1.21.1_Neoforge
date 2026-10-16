package net.aelysium.aelysiummod.sistemas.rp.raca.item;

import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;


import java.util.List;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;

public class Bateria extends Item {
    private static final SoundEvent SOM_SILENCIO = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("aelysiummod", "silencio"));
    private static final SoundEvent SOM_CHOQUE = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("aelysiummod", "bateria.choque"));

    public Bateria(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (PerfilJogador.raca().raca(player.getUUID()) != TipoRaca.ROBO) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("mensagem.aelysiummod.raca.somente_jogadores_da_raca_robo_podem"));
            }
            return InteractionResultHolder.fail(stack);
        }

        if (player.getFoodData().needsFood() || player.getFoodData().getSaturationLevel() < 20.0f) {
            if (!level.isClientSide) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SOM_CHOQUE, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SOM_SILENCIO;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SOM_SILENCIO;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level instanceof net.minecraft.server.level.ServerLevel nivel) {
            var pacote = new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(SOM_CHOQUE.getLocation(), SoundSource.PLAYERS);
            for (var jogador : nivel.players()) {
                if (jogador.distanceToSqr(entity) < 64 * 64) jogador.connection.send(pacote);
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            player.getFoodData().eat(5, 5.0f);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.raca.serve_para_re_energizar_maquinas")
                .withStyle(Style.EMPTY.withColor(0x55FF55).withItalic(true)));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
