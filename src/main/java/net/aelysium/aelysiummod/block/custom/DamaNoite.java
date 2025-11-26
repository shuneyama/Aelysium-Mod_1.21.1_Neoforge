package net.aelysium.aelysiummod.block.custom;

import net.aelysium.aelysiummod.particula.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class DamaNoite extends FlowerBlock {

    private int particleCooldown = 0;

    public DamaNoite(Holder<MobEffect> effect, float seconds, Properties properties) {
        super(effect, seconds, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        if (!level.isClientSide) return;

        if (particleCooldown > 0) {
            particleCooldown--;
            return;
        }

        AABB bb = state.getShape(level, pos).bounds();
        double centerX = pos.getX() + (bb.minX + bb.maxX) / 2.0;
        double centerY = pos.getY() + bb.maxY; // bem em cima da flor
        double centerZ = pos.getZ() + (bb.minZ + bb.maxZ) / 2.0;

        double radius = 0.1875;
        double offsetX = (random.nextDouble() * 2 - 1) * radius;
        double offsetZ = (random.nextDouble() * 2 - 1) * radius;

        double speedY = 0.01 + random.nextDouble() * 0.01;
        double speedX = (random.nextDouble() * 2 - 1) * 0.005;
        double speedZ = (random.nextDouble() * 2 - 1) * 0.005;

        level.addParticle(ModParticles.DAMA_DA_NOITE_PARTICULA.get(),
                centerX + offsetX,
                centerY + 0.15,
                centerZ + offsetZ,
                speedX, speedY, speedZ);

        particleCooldown = 5;
    }
}
