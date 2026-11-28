package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.hitbox;

import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.EfeitosAtaque;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.VoidBlossomEntity;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.dano.IDamageHandler;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.IEntityStats;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.IEntityTick;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade.EntityBounds;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade.EntityPart;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade.MutableBox;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class VoidBlossomCompoundHitbox implements ICompoundHitbox, IDamageHandler, IEntityTick<ServerLevel> {
    private final VoidBlossomEntity entity;
    private final EntityBounds hitboxes;
    private final String root;
    private final AABB collisionHitbox;
    private final List<String> spikedBoxes;

    private String nextDamagedPart;

    public VoidBlossomCompoundHitbox(VoidBlossomEntity entity, EntityBounds hitboxes, String root, AABB collisionHitbox, List<String> spikedBoxes) {
        this.entity = entity;
        this.hitboxes = hitboxes;
        this.root = root;
        this.collisionHitbox = collisionHitbox;
        this.spikedBoxes = spikedBoxes;
    }

    @Override
    public void updatePosition() {
        EntityPart rootYaw = hitboxes.getPart(root);
        rootYaw.setRotation(0.0, -entity.getYRot(), 0.0, true);

        rootYaw.setX(entity.getX());
        rootYaw.setY(entity.getY());
        rootYaw.setZ(entity.getZ());

        MutableBox overrideBox = hitboxes.getOverrideBox();
        if (overrideBox != null) overrideBox.setBox(collisionHitbox.move(entity.position()).move(-1.0, 0.0, -1.0));
    }

    @Override
    public EntityBounds getBounds() {
        return hitboxes;
    }

    @Override
    public void setNextDamagedPart(String part) {
        nextDamagedPart = part;
    }

    @Override
    public void beforeDamage(IEntityStats stats, DamageSource damageSource, float amount) {}

    @Override
    public void afterDamage(IEntityStats stats, DamageSource damageSource, float amount, boolean result) {
        String part = nextDamagedPart;
        nextDamagedPart = null;

        if (result)
            if (spikedBoxes.contains(part) && !damageSource.is(DamageTypeTags.IS_PROJECTILE)){
                if (damageSource.getEntity() != null)
                    EfeitosAtaque.ferir(entity, damageSource.getEntity(), entity.level().damageSources().thorns(entity), ConfiguracaoChefes.ataque(ConfiguracaoChefes.FLOR_CONTATO));
            }
    }

    @Override
    public boolean shouldDamage(LivingEntity actor, DamageSource damageSource, float amount) {
        return true;
    }

    @Override
    public void tick(ServerLevel level) {
        EntityPart rootYaw = hitboxes.getPart(root);
        rootYaw.setRotation(0.0, -entity.getYRot(), 0.0, true);
    }
}
