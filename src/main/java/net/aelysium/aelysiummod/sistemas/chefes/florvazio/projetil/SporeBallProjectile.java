package net.aelysium.aelysiummod.sistemas.chefes.florvazio.projetil;

import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.EfeitosAtaque;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.TiposDanoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.AgendadorNivel;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.RegistroFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.comum.RiftBurst;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.util.BMDUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.EventScheduler;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.TimedEvent;
import net.aelysium.aelysiummod.sistemas.chefes.base.particula.ClientParticleBuilder;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SporeBallProjectile extends BaseThrownItemProjectile {
    private final List<Vec3> circlePoints = MathUtils.buildBlockCircle(7.0);
    private final ClientParticleBuilder projectileParticles = new ClientParticleBuilder(RegistroFlorVazio.DISAPPEARING_SWIRL.get())
            .color(Vec3Colors.GREEN)
            .colorVariation(0.4)
            .scale(0.2f)
            .brightness(RegistroFlorVazio.BRILHO_TOTAL);
    private final byte particle = 5;
    private float impactedPitch = 0f;

    public float impactedTicks = 0f;
    public boolean impacted = false;

    public static final int explosionDelay = 15;

    public SporeBallProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
        collisionPredicate = hitResult -> true;
    }

    public SporeBallProjectile(LivingEntity livingEntity, Level level, Predicate<EntityHitResult> entityPredicate){
        super(RegistroFlorVazio.SPORE_BALL.get(), livingEntity, level, entityPredicate);
        collisionPredicate = hitResult -> true;
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        onImpact();
    }

    @Override
    public void clientTick() {
        super.clientTick();
        if (impacted)
            impactedTicks++;
    }

    @Override
    public @NotNull Vec3 getDeltaMovement() {
        return !impacted ? super.getDeltaMovement() : Vec3.ZERO;
    }

    private void onImpact(){
        if (impacted) return;

        impactedPitch = getXRot();
        impacted = true;
        Entity owner = getOwner();
        if (owner instanceof LivingEntity livingEntity)
            doExplosion(livingEntity);
        else if (!level().isClientSide())
            discard();
    }

    @Override
    public float getXRot() {
        return impacted ? impactedPitch : tickCount * 5f;
    }

    @Override
    public float getYRot() {
        return 0f;
    }

    private void doExplosion(LivingEntity owner){
        level().broadcastEntityEvent(this, particle);
        playSound(RegistroFlorVazio.SPORE_BALL_LAND.get(), 1.0f, SoundUtils.randomPitch(random) - 0.2f);
        EventScheduler eventScheduler = AgendadorNivel.de(level());
        Consumer<LivingEntity> onImpact = entity -> {
            EfeitosAtaque.ferir(owner, entity, TiposDanoChefes.veneno(level(), this, owner), ConfiguracaoChefes.ataque(ConfiguracaoChefes.FLOR_ESPOROS));
        };

        if (!level().isClientSide) {
            RiftBurst riftBurst = new RiftBurst(
                    owner,
                    (ServerLevel) level(),
                    RegistroFlorVazio.SPORE_INDICATOR.get(),
                    RegistroFlorVazio.SPORE.get(),
                    explosionDelay,
                    eventScheduler,
                    onImpact,
                    this::isOpenBlock,
                    this::posFinder
            );

            eventScheduler.addEvent(
                    new TimedEvent(
                            () -> {
                                playSound(RegistroFlorVazio.SPORE_IMPACT.get(), 1.5f, SoundUtils.randomPitch(random));
                                discard();
                            },
                            explosionDelay
                    )
            );

            Vec3 center = VecUtils.asVec3(blockPosition()).add(VecUtils.unit.scale(0.5));
            for (Vec3 point : circlePoints)
                riftBurst.tryPlaceRift(center.add(point));
        }
    }

    private BlockPos posFinder(Vec3 pos){
        BlockPos above = BlockPos.containing(pos.add(VecUtils.yAxis.scale(2.0)));
        BlockPos groundPos = BMDUtils.findGroundBelow(level(), above, pos1 -> true);
        BlockPos up = groundPos.above();
        return (up.getY() + 8 >= above.getY() && isOpenBlock(up)) ? up : null;
    }

    private boolean isOpenBlock(BlockPos up) {
        BlockState blockState = level().getBlockState(up);
        return blockState.canBeReplaced(new DirectionalPlaceContext(
                level(),
                up,
                Direction.DOWN,
                ItemStack.EMPTY,
                Direction.UP
        )) || blockState.getBlock() == Blocks.MOSS_CARPET;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == particle)
            for (Vec3 point : MathUtils.circlePoints(0.8, 16, VecUtils.yAxis))
                projectileParticles.build(point.add(position()), point.scale(0.1));

        super.handleEntityEvent(id);
    }

    @Override
    public void entityHit(EntityHitResult entityHitResult) {
        if (level().isClientSide()) return;
        Entity owner = getOwner();
        Entity entity = entityHitResult.getEntity();

        if (owner instanceof LivingEntity livingEntity)
            if (entity != livingEntity){
                EfeitosAtaque.ferir(livingEntity, entity, TiposDanoChefes.veneno(level(), this, livingEntity), ConfiguracaoChefes.ataque(ConfiguracaoChefes.FLOR_ESPOROS));
            }
    }
}
