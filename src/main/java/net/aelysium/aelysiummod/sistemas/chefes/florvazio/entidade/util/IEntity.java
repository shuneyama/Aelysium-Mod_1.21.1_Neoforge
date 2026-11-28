package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util;

import net.minecraft.world.phys.Vec3;

public interface IEntity {
    Vec3 getDeltaMovement();
    Vec3 getPos();
    Vec3 getEyePos();
    Vec3 getLookAngle();
    int getTickCount();
    boolean isAlive();
    IEntity target();
}
