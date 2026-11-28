package net.aelysium.aelysiummod.sistemas.chefes.base.particula;

import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.RandomUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.VecUtils;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class SimpleParticle extends TextureSheetParticle {
   private final ParticleContext particleContext;
   private final IParticleGeometry particleGeometry;
   private final boolean cycleSprites;
   private Function<SimpleParticle, Float> rotationOverride;
   private Function<SimpleParticle, Vec3> velocityOverride;
   private Function<SimpleParticle, Vec3> positionOverride;
   private Function<Float, Float> scaleOverride;
   private Function<Float, Vec3> colorOverride;
   private Function<Float, Integer> brightnessOverride;
   private Vec3 colorVariation = Vec3.ZERO;
   private float prevRotation = 0.0F;
   private float rotation = 0.0F;
   public float ageRatio = 1.0F;

   public SimpleParticle(ParticleContext particleContext, int particleAge, IParticleGeometry particleGeometry, boolean cycleSprites, boolean doCollision) {
      super(particleContext.level(), particleContext.pos().x(), particleContext.pos().y(), particleContext.pos().z());
      this.particleContext = particleContext;
      this.lifetime = particleAge;
      this.particleGeometry = particleGeometry;
      this.cycleSprites = cycleSprites;
      this.hasPhysics = doCollision;
      this.xd = particleContext.vel().x();
      this.yd = particleContext.vel().y();
      this.zd = particleContext.vel().z();
      if (cycleSprites) {
         this.setSpriteFromAge(particleContext.spriteSet());
      } else {
         this.setSprite(particleContext.spriteSet().get(this.random));
      }
   }

   @NotNull
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   @NotNull
   public Vec3 getPos() {
      return new Vec3(this.x, this.y, this.z);
   }

   public int getAge() {
      return this.age;
   }

   public void tick() {
      super.tick();
      if (this.isAlive()) {
         if (this.cycleSprites) {
            this.setSpriteFromAge(this.particleContext.spriteSet());
         }

         this.ageRatio = (float)this.age / (float)this.lifetime;
         this.setColorFromOverride(this.colorOverride, this.ageRatio);
         this.setScaleFromOverride(this.scaleOverride, this.ageRatio);
         this.setVelocityFromOverride(this.velocityOverride);
         this.setPositionFromOverride(this.positionOverride);
         this.setRotationFromOverride(this.rotationOverride);
      }
   }

   private void setRotationFromOverride(Function<SimpleParticle, Float> rotationOverride) {
      if (rotationOverride != null) {
         float rot = rotationOverride.apply(this);
         this.prevRotation = this.rotation;
         this.rotation = rot;
      }
   }

   private void setVelocityFromOverride(Function<SimpleParticle, Vec3> velocityOverride) {
      if (velocityOverride != null) {
         Vec3 velocity = velocityOverride.apply(this);
         this.xd = velocity.x();
         this.yd = velocity.y();
         this.zd = velocity.z();
      }
   }

   private void setPositionFromOverride(Function<SimpleParticle, Vec3> positionOverride) {
      if (positionOverride != null) {
         Vec3 pos = positionOverride.apply(this);
         this.setPos(pos.x(), pos.y(), pos.z());
      }
   }

   private void setScaleFromOverride(Function<Float, Float> scaleOverride, float ageRatio) {
      if (scaleOverride != null) {
         this.quadSize = scaleOverride.apply(ageRatio);
         this.setSize(0.2F * this.quadSize, 0.2F * this.quadSize);
      }
   }

   private void setColorFromOverride(Function<Float, Vec3> colorOverride, float ageRatio) {
      if (colorOverride != null) {
         Vec3 color = colorOverride.apply(ageRatio);
         Vec3 variedColor = VecUtils.coerceAtMost(VecUtils.coerceAtLeast(color.add(this.colorVariation), Vec3.ZERO), VecUtils.unit);
         this.setColor((float)variedColor.x(), (float)variedColor.y(), (float)variedColor.z());
      }
   }

   public void setBrightnessOverride(Function<Float, Integer> override) {
      this.brightnessOverride = override;
   }

   public void setColorOverride(Function<Float, Vec3> override) {
      this.colorOverride = override;
      this.setColorFromOverride(override, 0.0F);
   }

   public void setScaleOverride(Function<Float, Float> override) {
      this.scaleOverride = override;
      this.setScaleFromOverride(override, 0.0F);
   }

   public void setColorVariation(double variation) {
      this.colorVariation = RandomUtils.randVec().scale(variation);
      this.setColorFromOverride(this.colorOverride, 0.0F);
   }

   public void setVelocityOverride(Function<SimpleParticle, Vec3> override) {
      this.velocityOverride = override;
   }

   public void setPositionOverride(Function<SimpleParticle, Vec3> override) {
      this.positionOverride = override;
   }

   public void setRotationOverride(Function<SimpleParticle, Float> override) {
      this.rotationOverride = override;
      if (this.rotationOverride != null) {
         this.rotation = this.rotationOverride.apply(this);
         this.prevRotation = this.rotationOverride.apply(this);
      }
   }

   protected int getLightColor(float partialTick) {
      return this.brightnessOverride != null ? this.brightnessOverride.apply(this.ageRatio) : super.getLightColor(partialTick);
   }

   public void render(VertexConsumer vertexConsumer, @NotNull Camera camera, float partialTicks) {
      Vector3f[] vector3fs = this.particleGeometry
         .getGeometry(
            camera,
            partialTicks,
            this.xo,
            this.yo,
            this.zo,
            this.x,
            this.y,
            this.z,
            this.getQuadSize(partialTicks),
            Mth.lerp(partialTicks, this.prevRotation, this.rotation)
         );
      float l = this.getU0();
      float m = this.getU1();
      float n = this.getV0();
      float o = this.getV1();
      float p = (float)this.getLightColor(partialTicks);
      vertexConsumer.addVertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z())
         .setUv(m, o)
         .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
         .setLight((int)p);
      vertexConsumer.addVertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z())
         .setUv(m, n)
         .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
         .setLight((int)p);
      vertexConsumer.addVertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z())
         .setUv(l, n)
         .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
         .setLight((int)p);
      vertexConsumer.addVertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z())
         .setUv(l, o)
         .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
         .setLight((int)p);
   }
}
