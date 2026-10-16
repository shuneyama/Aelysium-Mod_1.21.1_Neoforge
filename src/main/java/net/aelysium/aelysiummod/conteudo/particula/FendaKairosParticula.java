package net.aelysium.aelysiummod.conteudo.particula;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class FendaKairosParticula extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected FendaKairosParticula(ClientLevel level, double x, double y, double z,
                                    double vx, double vy, double vz, SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz);

        this.sprites = spriteSet;
        this.gravity = 0.0F;
        this.friction = 0.96F;
        this.lifetime = 25 + this.random.nextInt(15);

        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        this.quadSize = 0.10F + this.random.nextFloat() * 0.06F;

        float warmth = this.random.nextFloat();
        float r = Mth.lerp(warmth, 0.55F, 0.92F);
        float g = Mth.lerp(warmth, 0.30F, 0.56F);
        float b = Mth.lerp(warmth, 0.10F, 0.20F);
        this.setColor(r, g, b);

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(this.sprites);
            this.quadSize += 0.008F;
            this.yd += 0.0025;

            float lifeRatio = (float) this.age / (float) this.lifetime;
            this.alpha = 1.0F - Mth.clamp(lifeRatio, 0.0F, 1.0F);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provedor implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provedor(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                        double x, double y, double z,
                                        double vx, double vy, double vz) {
            return new FendaKairosParticula(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
