package net.aelysium.aelysiummod.conteudo.particula;

import net.aelysium.aelysiummod.conteudo.item.arma.UltimoSuspiroItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class SangueParticula extends TextureSheetParticle {

    private int atraso;

    protected SangueParticula(ClientLevel level, double x, double y, double z,
                              double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);

        this.pickSprite(sprites);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.lifetime = UltimoSuspiroItem.SANGUE_VIDA;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.quadSize = 0.14F + this.random.nextFloat() * 0.12F;
        this.hasPhysics = true;
        this.atraso = this.random.nextInt(UltimoSuspiroItem.SANGUE_ATRASO_MAX + 1);
        this.alpha = 0.0F;
    }

    @Override
    public void tick() {
        if (this.atraso > 0) {
            this.atraso--;
            return;
        }

        this.alpha = 1.0F;
        super.tick();
        if (this.age > this.lifetime - 2) {
            this.alpha = Math.max(0.0F, (this.lifetime - this.age) / 2.0F);
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
            return new SangueParticula(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
