package net.aelysium.aelysiummod.sistemas.chefes.base.particula;

import net.minecraft.client.Camera;
import org.joml.Vector3f;

@FunctionalInterface
public interface IParticleGeometry {
   Vector3f[] getGeometry(Camera var1, float var2, double var3, double var5, double var7, double var9, double var11, double var13, float var15, float var16);
}
