package net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios;

import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Geometries {
   public static Vector3f[] buildBillBoardGeometry(
      @NotNull Camera camera, float tickDelta, double prevPosX, double prevPosY, double prevPosZ, double x, double y, double z, float scale, float rotation
   ) {
      Vec3 vec3 = camera.getPosition();
      float f = (float)(Mth.lerp((double)tickDelta, prevPosX, x) - vec3.x());
      float g = (float)(Mth.lerp((double)tickDelta, prevPosY, y) - vec3.y());
      float h = (float)(Mth.lerp((double)tickDelta, prevPosZ, z) - vec3.z());
      Quaternionf quaternion2 = camera.rotation();
      Vector3f[] vector3fs = new Vector3f[]{
         new Vector3f(1.0F, -1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(-1.0F, -1.0F, 0.0F)
      };

      for (int k = 0; k <= 3; k++) {
         Vector3f vector3f2 = vector3fs[k];
         vector3f2.rotate(Axis.ZP.rotationDegrees(rotation));
         vector3f2.rotate(quaternion2);
         vector3f2.mul(scale);
         vector3f2.add(f, g, h);
      }

      return vector3fs;
   }

   @NotNull
   public static Vector3f[] buildFlatGeometry(
      @NotNull Camera camera, float tickDelta, double prevPosX, double prevPosY, double prevPosZ, double x, double y, double z, float scale, float rotation
   ) {
      Vec3 vec3 = camera.getPosition();
      float f = (float)(Mth.lerp((double)tickDelta, prevPosX, x) - vec3.x());
      float g = (float)(Mth.lerp((double)tickDelta, prevPosY, y) - vec3.y());
      float h = (float)(Mth.lerp((double)tickDelta, prevPosZ, z) - vec3.z());
      Vector3f[] vector3fs = new Vector3f[]{
         new Vector3f(-1.0F, 0.0F, -1.0F), new Vector3f(-1.0F, 0.0F, 1.0F), new Vector3f(1.0F, 0.0F, 1.0F), new Vector3f(1.0F, 0.0F, -1.0F)
      };

      for (int k = 0; k <= 3; k++) {
         Vector3f vector3f2 = vector3fs[k];
         vector3f2.rotate(Axis.YP.rotationDegrees(rotation));
         vector3f2.mul(scale);
         vector3f2.add(f, g, h);
      }

      return vector3fs;
   }
}
