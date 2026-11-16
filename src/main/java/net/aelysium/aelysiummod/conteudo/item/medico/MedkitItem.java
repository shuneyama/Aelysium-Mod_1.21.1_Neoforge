package net.aelysium.aelysiummod.conteudo.item.medico;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class MedkitItem extends Item {

    private static final int DURACAO_USO = 20 * 20;

    public MedkitItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return DURACAO_USO;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!precisaDeTratamento(player)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("mensagem.aelysiummod.medkit.sem_necessidade"), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide || remainingUseDuration % 20 != 0 || !(livingEntity instanceof Player player)) return;

        player.displayClientMessage(Component.translatable("mensagem.aelysiummod.medkit.usando", remainingUseDuration / 20), true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.4f, 1.2f);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.medkit.interrompido"), true);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (level.isClientSide) return stack;

        livingEntity.heal(livingEntity.getMaxHealth() - livingEntity.getHealth());
        efeitosNegativos(livingEntity).forEach(livingEntity::removeEffect);

        level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.5f);

        if (livingEntity instanceof Player player) {
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.medkit.concluido"), true);
        }

        stack.consume(1, livingEntity);
        return stack;
    }

    private static boolean precisaDeTratamento(LivingEntity entidade) {
        return entidade.getHealth() < entidade.getMaxHealth() || !efeitosNegativos(entidade).isEmpty();
    }

    private static List<Holder<MobEffect>> efeitosNegativos(LivingEntity entidade) {
        return entidade.getActiveEffects().stream()
                .map(MobEffectInstance::getEffect)
                .filter(efeito -> efeito.value().getCategory() == MobEffectCategory.HARMFUL)
                .toList();
    }
}
