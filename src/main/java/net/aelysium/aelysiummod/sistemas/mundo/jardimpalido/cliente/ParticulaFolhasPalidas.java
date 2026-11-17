package net.aelysium.aelysiummod.sistemas.mundo.jardimpalido.cliente;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.CherryParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class ParticulaFolhasPalidas extends CherryParticle {

    protected ParticulaFolhasPalidas(ClientLevel mundo, double x, double y, double z, SpriteSet sprites) {
        super(mundo, x, y, z, sprites);
    }

    public static class Provedor implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provedor(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType tipo, ClientLevel mundo, double x, double y, double z, double vx, double vy, double vz) {
            return new ParticulaFolhasPalidas(mundo, x, y, z, sprites);
        }
    }
}
