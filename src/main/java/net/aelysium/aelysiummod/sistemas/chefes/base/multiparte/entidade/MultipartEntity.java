package net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.util.CompoundOrientedBox;
import net.minecraft.world.phys.AABB;

public interface MultipartEntity {
   CompoundOrientedBox getCompoundBoundingBox(AABB var1);
}
