package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.particula;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.particula.DadosNuvem;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render.MMRenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import org.jetbrains.annotations.NotNull;

public class ParticleCloud extends TextureSheetParticle {
    private final float red;
    private final float green;
    private final float blue;
    private final float scale;
    private final DadosNuvem.EnumCloudBehavior behavior;
    private final float airDrag;

    public ParticleCloud(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, double r, double g, double b, double scale, int duration, DadosNuvem.EnumCloudBehavior behavior, double airDrag) {
        super(world, x, y, z);
        this.scale = (float) scale * 0.5f * 0.1f;
        lifetime = duration;
        xd = vx * 0.5;
        yd = vy * 0.5;
        zd = vz * 0.5;
        red = (float) r;
        green = (float) g;
        blue = (float) b;
        this.behavior = behavior;
        roll = oRoll = (float) (random.nextInt(4) * Math.PI/2);
        this.airDrag = (float) airDrag;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return MMRenderType.PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH;
    }

    @Override
    public void tick() {
        super.tick();
        xd *= airDrag;
        yd *= airDrag;
        zd *= airDrag;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        float var = (age + partialTicks)/(float)lifetime;
        alpha = 0.2f * ((float) (1 - Math.exp(5 * (var - 1)) - Math.pow(2000, -var)));
        if (alpha < 0.01) alpha = 0.01f;
        if (behavior == DadosNuvem.EnumCloudBehavior.SHRINK) this.quadSize = scale * ((1 - 0.7f * var) + 0.3f);
        else if (behavior == DadosNuvem.EnumCloudBehavior.GROW) this.quadSize = scale * ((0.7f * var) + 0.3f);
        else this.quadSize = scale;

        super.render(buffer, renderInfo, partialTicks);
    }

    public static final class Provider implements ParticleProvider<DadosNuvem> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(DadosNuvem typeIn, @NotNull ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ParticleCloud particleCloud = new ParticleCloud(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.red(), typeIn.green(), typeIn.blue(), typeIn.scale(), typeIn.duration(), typeIn.behavior(), typeIn.airDrag());
            particleCloud.setSpriteFromAge(spriteSet);
            particleCloud.setColor(typeIn.red(), typeIn.green(), typeIn.blue());
            return particleCloud;
        }
    }

}
