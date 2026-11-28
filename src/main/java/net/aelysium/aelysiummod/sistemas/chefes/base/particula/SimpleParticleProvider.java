package net.aelysium.aelysiummod.sistemas.chefes.base.particula;

import java.util.function.Function;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleParticleProvider implements ParticleProvider<SimpleParticleType> {
   private final SpriteSet spriteSet;
   private final Function<ParticleContext, Particle> particleProvider;

   public SimpleParticleProvider(SpriteSet spriteSet, Function<ParticleContext, Particle> particleProvider) {
      this.spriteSet = spriteSet;
      this.particleProvider = particleProvider;
   }

   @Nullable
   public Particle createParticle(
      @NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed
   ) {
      return this.particleProvider.apply(new ParticleContext(this.spriteSet, level, new Vec3(x, y, z), new Vec3(xSpeed, ySpeed, zSpeed), true));
   }
}
