package net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade;

import net.minecraft.world.phys.AABB;

public final class MutableBox {
   private AABB box;

   public MutableBox(AABB box) {
      this.box = box;
   }

   public AABB getBox() {
      return this.box;
   }

   public void setBox(AABB box) {
      this.box = box;
   }
}
