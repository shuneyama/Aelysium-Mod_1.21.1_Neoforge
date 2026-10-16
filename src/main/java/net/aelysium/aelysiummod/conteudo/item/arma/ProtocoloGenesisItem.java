package net.aelysium.aelysiummod.conteudo.item.arma;

import net.aelysium.aelysiummod.registro.ModEfeitos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class ProtocoloGenesisItem extends SwordItem {

    private static final int USE_DURATION = 72000;
    private static final int TICKS_PARA_LIBERAR = 60;
    private static final int CHOQUE_DURACAO_TICKS = 100;
    private static final int CARGA_DURACAO_TICKS = 12000;

    public ProtocoloGenesisItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        int decorridos = getUseDuration(stack, livingEntity) - remainingUseDuration;
        if (decorridos < TICKS_PARA_LIBERAR) return;
        if (livingEntity.hasEffect(ModEfeitos.GENESIS_CARREGADO)) return;

        livingEntity.addEffect(new MobEffectInstance(ModEfeitos.GENESIS_CARREGADO, CARGA_DURACAO_TICKS, 0, false, true, true));
        livingEntity.stopUsingItem();

        level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.0f, 1.0f);

        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    livingEntity.getX(), livingEntity.getY() + livingEntity.getBbHeight() / 2, livingEntity.getZ(),
                    40, 0.6, 0.6, 0.6, 0.05);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.hasEffect(ModEfeitos.GENESIS_CARREGADO)) {
            attacker.removeEffect(ModEfeitos.GENESIS_CARREGADO);
            target.addEffect(new MobEffectInstance(ModEfeitos.CHOQUE, CHOQUE_DURACAO_TICKS, 0, false, false, true));
        }

        return super.hurtEnemy(stack, target, attacker);
    }
}
