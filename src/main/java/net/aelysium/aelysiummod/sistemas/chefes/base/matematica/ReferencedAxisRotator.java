package net.aelysium.aelysiummod.sistemas.chefes.base.matematica;

import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.VecUtils;
import net.minecraft.world.phys.Vec3;

public class ReferencedAxisRotator {
   private final double angleBetween;
   private final Vec3 rotationAxis;

   public ReferencedAxisRotator(Vec3 originalAxis, Vec3 newAxis) {
      this.angleBetween = VecUtils.unsignedAngle(originalAxis, newAxis);
      this.rotationAxis = originalAxis.cross(newAxis);
   }

   public Vec3 rotate(Vec3 vec) {
      return VecUtils.rotateVector(vec, this.rotationAxis, this.angleBetween);
   }
}
