package net.aelysium.aelysiummod.conteudo.item.cigarro;

import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IsqueiroItem extends Item {

    public static final int TICKS_PARA_TAMPA = 20;
    private static final int USE_DURATION = 72000;
    private static final int COOLDOWN_TAMPA_TICKS = 20;

    public IsqueiroItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        int decorridos = getUseDuration(stack, livingEntity) - remainingUseDuration;
        if (decorridos < TICKS_PARA_TAMPA) return;

        boolean tampaAberta = stack.getOrDefault(ModComponentesDados.ISQUEIRO_TAMPA_ABERTA, false);
        boolean aceso = stack.getOrDefault(ModComponentesDados.ISQUEIRO_ACESO, false);

        if (tampaAberta && aceso) {
            livingEntity.stopUsingItem();
            return;
        }

        stack.set(ModComponentesDados.ISQUEIRO_TAMPA_ABERTA, !tampaAberta);
        stack.set(ModComponentesDados.ISQUEIRO_TAMPA_TICK, level.getGameTime());

        level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 0.7f, 1.0f);

        livingEntity.stopUsingItem();

        if (livingEntity instanceof Player player) {
            player.getCooldowns().addCooldown(this, COOLDOWN_TAMPA_TICKS);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        int decorridos = getUseDuration(stack, livingEntity) - timeLeft;
        if (decorridos >= TICKS_PARA_TAMPA) return;

        boolean tampaAberta = stack.getOrDefault(ModComponentesDados.ISQUEIRO_TAMPA_ABERTA, false);
        if (!tampaAberta) return;

        boolean aceso = stack.getOrDefault(ModComponentesDados.ISQUEIRO_ACESO, false);
        stack.set(ModComponentesDados.ISQUEIRO_ACESO, !aceso);

        level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.PLAYERS, 0.7f, 1.0f);
    }
}
