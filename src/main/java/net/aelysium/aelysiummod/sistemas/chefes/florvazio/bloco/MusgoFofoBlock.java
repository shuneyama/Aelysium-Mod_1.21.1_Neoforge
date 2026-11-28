package net.aelysium.aelysiummod.sistemas.chefes.florvazio.bloco;

import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MusgoFofoBlock extends PowderSnowBlock {
    private static final Vec3 LENTIDAO = new Vec3(0.9F, 1.5, 0.9F);
    private static final int RENOVAR_ABAIXO_DE = 10;

    public MusgoFofoBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
        if (!(entity instanceof LivingEntity) || entity.getInBlockState().is(this)) {
            entity.makeStuckInBlock(state, LENTIDAO);
            if (level.isClientSide) {
                RandomSource aleatorio = level.getRandom();
                boolean moveu = entity.xOld != entity.getX() || entity.zOld != entity.getZ();
                if (moveu && aleatorio.nextBoolean()) {
                    level.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, entity.getX(), pos.getY() + 1, entity.getZ(),
                            Mth.randomBetween(aleatorio, -1.0F, 1.0F) * 0.083333336F, 0.05F, Mth.randomBetween(aleatorio, -1.0F, 1.0F) * 0.083333336F);
                }
            }
        }
        if (!level.isClientSide && entity instanceof LivingEntity vivo) envenenar(vivo);
    }

    private static void envenenar(LivingEntity vivo) {
        int nivel = ConfiguracaoChefes.inteiro(ConfiguracaoChefes.FLOR + ".musgo_fofo.nivel_veneno");
        int duracao = (int) Math.round(ConfiguracaoChefes.numero(ConfiguracaoChefes.FLOR + ".musgo_fofo.duracao_veneno_segundos") * 20);
        if (nivel <= 0 || duracao <= 0) return;
        MobEffectInstance atual = vivo.getEffect(MobEffects.POISON);
        if (atual != null && atual.getAmplifier() >= nivel - 1 && atual.getDuration() > RENOVAR_ABAIXO_DE) return;
        vivo.addEffect(new MobEffectInstance(MobEffects.POISON, duracao, nivel - 1));
    }

    @Override
    public @NotNull ItemStack pickupBlock(Player player, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull Optional<SoundEvent> getPickupSound() {
        return Optional.empty();
    }
}
