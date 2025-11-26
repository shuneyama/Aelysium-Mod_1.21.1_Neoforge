package net.aelysium.aelysiummod.particula;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class DamaVermelhaDaNoiteParticula extends TextureSheetParticle {
    private final SpriteSet sprites;


    protected DamaVermelhaDaNoiteParticula(ClientLevel level, double x, double y, double z,
                                           double vx, double vy, double vz, SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz);

        this.setSpriteFromAge(spriteSet);
        this.sprites = spriteSet;
        this.lifetime = 30;
        this.friction = 0.9f;
        this.quadSize = 0.025F + (random.nextFloat() * 0.025F);
        this.xd *= 0.2;
        this.yd *= 0.2;
        this.zd *= 0.2;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.xd *= 0.9;
            this.zd *= 0.9;
            this.yd = Math.abs(this.yd);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new DamaVermelhaDaNoiteParticula(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}