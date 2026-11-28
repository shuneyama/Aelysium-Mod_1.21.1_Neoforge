package net.aelysium.aelysiummod.sistemas.chefes.florvazio.projetil;

import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.EfeitosAtaque;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.RegistroFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.projetil.util.ExemptEntities;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class PetalBladeProjectile extends BaseThrownItemProjectile{
    private Consumer<LivingEntity> entityHit;

    public static final EntityDataAccessor<Float> renderRotation = SynchedEntityData.defineId(PetalBladeProjectile.class, EntityDataSerializers.FLOAT);

    public PetalBladeProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
        getEntityData().set(renderRotation, 0f);
    }

    public PetalBladeProjectile(LivingEntity livingEntity, Level level, Consumer<LivingEntity> entityHit, List<EntityType<?>> exemptEntities, float rotation){
        super(RegistroFlorVazio.PETAL_BLADE.get(), livingEntity, level, new ExemptEntities(exemptEntities));
        this.entityHit = entityHit;
        getEntityData().set(renderRotation, rotation);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(renderRotation, 0f);
    }

    @Override
    public void entityHit(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        Entity owner = getOwner();

        if (owner instanceof LivingEntity livingEntity){
            EfeitosAtaque.ferir(livingEntity, entity, entity.level().damageSources().thrown(this, owner), ConfiguracaoChefes.ataque(ConfiguracaoChefes.FLOR_LAMINAS));

            if (entity instanceof LivingEntity)
                if (entityHit != null)
                    entityHit.accept((LivingEntity) entity);
        }
        discard();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        discard();
    }
}
