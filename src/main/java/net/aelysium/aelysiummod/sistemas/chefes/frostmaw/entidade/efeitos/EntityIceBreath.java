package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.efeitos;

import net.aelysium.aelysiummod.sistemas.chefes.base.combate.AtaqueChefe;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.EfeitosAtaque;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.TiposDanoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.particula.DadosFlocoNeve;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.particula.DadosAnel;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.particula.DadosNuvem;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.RegistroFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ComumFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.EntityFrostmaw;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class EntityIceBreath extends EntityMagicEffect {
    private static final int RANGE = 10;
    private static final int ARC = 45;

    public EntityIceBreath(EntityType<? extends EntityIceBreath> type, Level world) {
        super(type, world);
    }

    public EntityIceBreath(EntityType<? extends EntityIceBreath> type, Level world, LivingEntity caster) {
        super(type, world, caster);
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount == 1) {
            if (level().isClientSide) {
                ComumFrostmaw.PROXY.playIceBreathSound(this);
            }
        }
        if (tickCount > 1 && getCaster() == null) this.discard() ;
        if (getCaster() != null && !getCaster().isAlive()) this.discard() ;
        if (tickCount == 1) playSound(RegistroFrostmaw.ENTITY_FROSTMAW_ICEBREATH_START.get(), 1, 0.6f);

        float yaw = (float) Math.toRadians(-getYRot());
        float pitch = (float) Math.toRadians(-getXRot());
        float spread = 0.25f;
        float speed = 0.56f;
        float xComp = (float) (Math.sin(yaw) * Math.cos(pitch));
        float yComp = (float) (Math.sin(pitch));
        float zComp = (float) (Math.cos(yaw) * Math.cos(pitch));
        if (level().isClientSide) {
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            boolean overrideLimiter = camera.getPosition().distanceToSqr(getX(), getY(), getZ()) < 64 * 64;
            if (tickCount % 8 == 0) {
                level().addAlwaysVisibleParticle(new DadosAnel(yaw, -pitch, 40, 1f, 1f, 1f, 1f, (int) (110 * spread), false, DadosAnel.EnumRingBehavior.GROW), overrideLimiter, getX(), getY(), getZ(), 0.5f * xComp, 0.5f * yComp, 0.5f * zComp);
            }

            for (int i = 0; i < 6; i++) {
                double xSpeed = speed * 1f * xComp;
                double ySpeed = speed * 1f * yComp;
                double zSpeed = speed * 1f * zComp;
                level().addParticle(new DadosFlocoNeve(37f, true), getX(), getY(), getZ(), xSpeed, ySpeed, zSpeed);
            }
            for (int i = 0; i < 5; i++) {
                double xSpeed = speed * xComp + (spread * 0.7 * (random.nextFloat() * 2 - 1) * (Math.sqrt(1 - xComp * xComp)));
                double ySpeed = speed * yComp + (spread * 0.7 * (random.nextFloat() * 2 - 1) * (Math.sqrt(1 - yComp * yComp)));
                double zSpeed = speed * zComp + (spread * 0.7 * (random.nextFloat() * 2 - 1) * (Math.sqrt(1 - zComp * zComp)));
                float value = random.nextFloat() * 0.15f;
                level().addAlwaysVisibleParticle(new DadosNuvem(0.75f + value, 0.75f + value,1f, 10f + random.nextFloat() * 20f, 40, DadosNuvem.EnumCloudBehavior.GROW, 1f), overrideLimiter, getX(), getY(), getZ(), xSpeed, ySpeed, zSpeed);
            }
        }
        if (tickCount > 10) hitEntities();
        if (tickCount > 10) freezeBlocks();

        if (tickCount > 65 && !(getCaster() instanceof Player)) discard() ;
    }

    public void hitEntities() {
        List<Entity> entitiesHit = getEntityLivingBaseNearby(RANGE, RANGE, RANGE, RANGE);
        AtaqueChefe ataque = ConfiguracaoChefes.ataque(ConfiguracaoChefes.FROSTMAW_SOPRO);
        for (Entity entityHit : entitiesHit) {
            if (entityHit instanceof ItemEntity) continue;
            if (entityHit == getCaster()) continue;
            if (entityHit instanceof ItemEntity) continue;

            if (entityHit.getType().is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES) || entityHit instanceof EnderDragon) continue;

            float entityHitYaw = (float) ((Math.atan2(entityHit.getZ() - getZ(), entityHit.getX() - getX()) * (180 / Math.PI) - 90) % 360);
            float entityAttackingYaw = getYRot() % 360;
            if (entityHitYaw < 0) {
                entityHitYaw += 360;
            }
            if (entityAttackingYaw < 0) {
                entityAttackingYaw += 360;
            }
            float entityRelativeYaw = entityHitYaw - entityAttackingYaw;

            float xzDistance = (float) Math.sqrt((entityHit.getZ() - getZ()) * (entityHit.getZ() - getZ()) + (entityHit.getX() - getX()) * (entityHit.getX() - getX()));
            double hitY = entityHit.getY() + entityHit.getBbHeight() / 2.0;
            float entityHitPitch = (float) ((Math.atan2((hitY - getY()), xzDistance) * (180 / Math.PI)) % 360);
            float entityAttackingPitch = -getXRot() % 360;
            if (entityHitPitch < 0) {
                entityHitPitch += 360;
            }
            if (entityAttackingPitch < 0) {
                entityAttackingPitch += 360;
            }
            float entityRelativePitch = entityHitPitch - entityAttackingPitch;

            float entityHitDistance = (float) Math.sqrt((entityHit.getZ() - getZ()) * (entityHit.getZ() - getZ()) + (entityHit.getX() - getX()) * (entityHit.getX() - getX()) + (hitY - getY()) * (hitY - getY()));

            boolean inRange = entityHitDistance <= RANGE;
            boolean yawCheck = (entityRelativeYaw <= ARC / 2f && entityRelativeYaw >= -ARC / 2f) || (entityRelativeYaw >= 360 - ARC / 2f || entityRelativeYaw <= -360 + ARC / 2f);
            boolean pitchCheck = (entityRelativePitch <= ARC / 2f && entityRelativePitch >= -ARC / 2f) || (entityRelativePitch >= 360 - ARC / 2f || entityRelativePitch <= -360 + ARC / 2f);
            boolean frostmawCloseCheck = getCaster() instanceof EntityFrostmaw && entityHitDistance <= 2;
            if (inRange && yawCheck && pitchCheck || frostmawCloseCheck) {
                if (!raytraceCheckEntity(entityHit)) continue;

                if (EfeitosAtaque.ferir(getCaster(), entityHit, TiposDanoChefes.congelamento(level(), this, getCaster()), ataque) && entityHit instanceof LivingEntity) {
                    entityHit.setDeltaMovement(entityHit.getDeltaMovement().multiply(0.25, 1, 0.25));
                    entityHit.getData(RegistroFrostmaw.FROZEN_DATA).addFreezeProgress((LivingEntity) entityHit, 0.23f);
                }
            }
        }
    }

    public void freezeBlocks() {
        int checkDist = 10;
        for (int i = (int)getX() - checkDist; i < (int)getX() + checkDist; i++) {
            for (int j = (int)getY() - checkDist; j < (int)getY() + checkDist; j++) {
                for (int k = (int)getZ() - checkDist; k < (int)getZ() + checkDist; k++) {
                    BlockPos pos = new BlockPos(i, j, k);

                    BlockState blockState = level().getBlockState(pos);
                    BlockState blockStateAbove = level().getBlockState(pos.above());
                    if (blockState.getBlock() != Blocks.WATER || blockStateAbove.getBlock() != Blocks.AIR) {
                        continue;
                    }

                    float blockHitYaw = (float) ((Math.atan2(pos.getZ() - getZ(), pos.getX() - getX()) * (180 / Math.PI) - 90) % 360);
                    float entityAttackingYaw = getYRot() % 360;
                    if (blockHitYaw < 0) {
                        blockHitYaw += 360;
                    }
                    if (entityAttackingYaw < 0) {
                        entityAttackingYaw += 360;
                    }
                    float blockRelativeYaw = blockHitYaw - entityAttackingYaw;

                    float xzDistance = (float) Math.sqrt((pos.getZ() - getZ()) * (pos.getZ() - getZ()) + (pos.getX() - getX()) * (pos.getX() - getX()));
                    float blockHitPitch = (float) ((Math.atan2((pos.getY() - getY()), xzDistance) * (180 / Math.PI)) % 360);
                    float entityAttackingPitch = -getXRot() % 360;
                    if (blockHitPitch < 0) {
                        blockHitPitch += 360;
                    }
                    if (entityAttackingPitch < 0) {
                        entityAttackingPitch += 360;
                    }
                    float blockRelativePitch = blockHitPitch - entityAttackingPitch;

                    float blockHitDistance = (float) Math.sqrt((pos.getZ() - getZ()) * (pos.getZ() - getZ()) + (pos.getX() - getX()) * (pos.getX() - getX()) + (pos.getY() - getY()) * (pos.getY() - getY()));

                    boolean inRange = blockHitDistance <= RANGE;
                    boolean yawCheck = (blockRelativeYaw <= ARC / 2f && blockRelativeYaw >= -ARC / 2f) || (blockRelativeYaw >= 360 - ARC / 2f || blockRelativeYaw <= -360 + ARC / 2f);
                    boolean pitchCheck = (blockRelativePitch <= ARC / 2f && blockRelativePitch >= -ARC / 2f) || (blockRelativePitch >= 360 - ARC / 2f || blockRelativePitch <= -360 + ARC / 2f);
                    if (inRange && yawCheck && pitchCheck) {
                        EntityBlockSwapper.swapBlock(level(), pos, Blocks.ICE.defaultBlockState(), 140, false, false);
                    }
                }
            }
        }
    }

    public  List<Entity> getEntityLivingBaseNearby(double distanceX, double distanceY, double distanceZ, double radius) {
        return getEntitiesNearby(Entity.class, distanceX, distanceY, distanceZ, radius);
    }

    public <T extends Entity> List<T> getEntitiesNearby(Class<T> entityClass, double dX, double dY, double dZ, double r) {
        return level().getEntitiesOfClass(entityClass, getBoundingBox().inflate(dX, dY, dZ), e -> e != this && distanceTo(e) <= r + e.getBbWidth() / 2f && e.getY() <= getY() + dY);
    }
}
