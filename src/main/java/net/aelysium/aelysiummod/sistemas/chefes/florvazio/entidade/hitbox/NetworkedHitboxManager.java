package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.hitbox;

import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.BaseEntity;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade.EntityBounds;
import net.minecraft.network.syncher.EntityDataAccessor;

import java.util.Map;

public class NetworkedHitboxManager implements ICompoundHitbox{
    private final BaseEntity entity;
    private final Map<Byte, ICompoundHitbox> hitboxMap;
    public final EntityDataAccessor<Byte> hitbox;

    public NetworkedHitboxManager(BaseEntity entity, Map<Byte, ICompoundHitbox> hitboxMap, EntityDataAccessor<Byte> hitbox) {
        this.entity = entity;
        this.hitboxMap = hitboxMap;
        this.hitbox = hitbox;
    }

    @Override
    public void updatePosition() {
        for (ICompoundHitbox hitbox1 : hitboxMap.values())
            hitbox1.updatePosition();
    }

    @Override
    public EntityBounds getBounds() {
        return hitboxMap.get(entity.getEntityData().get(hitbox)).getBounds();
    }

    @Override
    public void setNextDamagedPart(String part) {
        hitboxMap.get(entity.getEntityData().get(hitbox)).setNextDamagedPart(part);
    }
}
