package net.aelysium.aelysiummod.conteudo.item.medico;

import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;

public class SeringaCuraItem extends Item {

    private static final int DURACAO_USO = 3 * 20;
    private static final float CURA = 8.0f;
    private static final int INTERVALO_VERIFICACAO = 5;

    public SeringaCuraItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return DURACAO_USO;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;

        if (interactionTarget.getHealth() >= interactionTarget.getMaxHealth()) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.alvo_sem_necessidade"), true);
            }
            return InteractionResult.CONSUME;
        }

        player.startUsingItem(usedHand);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.dica"), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (player.getHealth() >= player.getMaxHealth()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.sem_necessidade"), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;

        if (remainingUseDuration % INTERVALO_VERIFICACAO == 0 && resolverAlvo(player) == null) {
            player.stopUsingItem();
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.alvo_perdido"), true);
            }
            return;
        }

        if (!level.isClientSide && remainingUseDuration % 20 == 0) {
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.aplicando", remainingUseDuration / 20), true);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.interrompida"), true);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) return stack;

        LivingEntity alvo = resolverAlvo(player);
        if (alvo == null) {
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.alvo_perdido"), true);
            return stack;
        }

        alvo.heal(CURA);
        level.playSound(null, alvo.getX(), alvo.getY(), alvo.getZ(),
                SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.6f, 1.6f);

        if (alvo == player) {
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.curou_si"), true);
        } else {
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.curou_alvo", alvo.getDisplayName()), true);
            if (alvo instanceof ServerPlayer jogadorAlvo) {
                jogadorAlvo.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.curado_por", player.getDisplayName()), true);
            }
        }

        return ItemUtils.createFilledResult(stack, player, new ItemStack(ModItens.SERINGA.get()));
    }

    private static LivingEntity resolverAlvo(Player player) {
        return player.isShiftKeyDown() ? player : SeringaItem.alvoNaMira(player);
    }
}
