package net.aelysium.aelysiummod.sistemas.chefes.base.particula;

import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.RandomUtils;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;

public class ClientParticleBuilder {
   private final ParticleOptions options;
   private Function<SimpleParticle, Vec3> getVel;
   private Function<SimpleParticle, Vec3> continuousPos;
   private Function<Float, Vec3> color;
   private Function<Float, Integer> brightness;
   private Function<Float, Float> scale;
   private Supplier<Integer> age;
   private Function<SimpleParticle, Float> getRotation;
   private Double colorVariation;

   public ClientParticleBuilder(ParticleOptions options) {
      this.options = options;
   }

   public ClientParticleBuilder continuousRotation(Function<SimpleParticle, Float> rotation) {
      this.getRotation = rotation;
      return this;
   }

   public ClientParticleBuilder continuousVelocity(Function<SimpleParticle, Vec3> velocity) {
      this.getVel = velocity;
      return this;
   }

   public ClientParticleBuilder continuousPosition(Function<SimpleParticle, Vec3> positionProvider) {
      this.continuousPos = positionProvider;
      return this;
   }

   public ClientParticleBuilder color(Function<Float, Vec3> color) {
      this.color = color;
      return this;
   }

   public ClientParticleBuilder color(Vec3 color) {
      this.color = f -> color;
      return this;
   }

   public ClientParticleBuilder rotation(float rotation) {
      this.getRotation = p -> rotation;
      return this;
   }

   public ClientParticleBuilder brightness(int brightness) {
      this.brightness = f -> brightness;
      return this;
   }

   public ClientParticleBuilder scale(Function<Float, Float> scale) {
      this.scale = scale;
      return this;
   }

   public ClientParticleBuilder scale(float scale) {
      this.scale = f -> scale;
      return this;
   }

   public ClientParticleBuilder age(Supplier<Integer> age) {
      this.age = age;
      return this;
   }

   public ClientParticleBuilder age(int age) {
      this.age = () -> age;
      return this;
   }

   public ClientParticleBuilder age(int min, int max) {
      this.age = () -> RandomUtils.range(min, max);
      return this;
   }

   public ClientParticleBuilder colorVariation(double variation) {
      this.colorVariation = variation;
      return this;
   }

   public void build(Vec3 pos, Vec3 vel) {
      Minecraft client = Minecraft.getInstance();
      Camera camera = client.gameRenderer.getMainCamera();
      if (camera.isInitialized()) {
         Particle particle = client.particleEngine.createParticle(this.options, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
         if (particle == null) {
            return;
         }

         if (this.scale != null) {
            particle.scale(this.scale.apply(0.0F));
         }

         if (this.color != null) {
            Vec3 clr = this.color.apply(0.0F);
            particle.setColor((float)clr.x, (float)clr.y, (float)clr.z);
         }

         if (this.age != null) {
            particle.setLifetime(this.age.get());
         }

         if (particle instanceof SimpleParticle simpleParticle) {
            if (this.brightness != null) {
               simpleParticle.setBrightnessOverride(this.brightness);
            }

            if (this.color != null) {
               simpleParticle.setColorOverride(this.color);
            }

            if (this.scale != null) {
               simpleParticle.setScaleOverride(this.scale);
            }

            if (this.getVel != null) {
               simpleParticle.setVelocityOverride(this.getVel);
            }

            if (this.continuousPos != null) {
               simpleParticle.setPositionOverride(this.continuousPos);
            }

            if (this.colorVariation != null) {
               simpleParticle.setColorVariation(this.colorVariation);
            }

            if (this.getRotation != null) {
               simpleParticle.setRotationOverride(this.getRotation);
            }
         }
      }
   }
}
