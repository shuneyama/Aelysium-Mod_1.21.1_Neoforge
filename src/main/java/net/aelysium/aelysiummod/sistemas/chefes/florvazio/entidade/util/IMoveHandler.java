package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util;

import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public interface IMoveHandler {
    boolean canMove(MoverType type, Vec3 movement);
}
