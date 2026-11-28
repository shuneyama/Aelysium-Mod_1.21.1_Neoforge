package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.hitbox;

import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade.EntityBounds;

public interface ICompoundHitbox {
    void updatePosition();
    EntityBounds getBounds();
    void setNextDamagedPart(String part);
}
