package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.particula;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.particula.DadosFlocoNeve;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.modelo.ferramentas.MathUtils;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render.MMRenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ParticleSnowFlake extends TextureSheetParticle {
    private int swirlTick;
    private final float spread;
    boolean swirls;

    public ParticleSnowFlake(ClientLevel world, double x, double y, double z, double vX, double vY, double vZ, double duration, boolean swirls) {
        super(world, x, y, z);
        setSize(1, 1);
        xd = vX;
        yd = vY;
        zd = vZ;
        lifetime = (int) duration;
        swirlTick = random.nextInt(120);
        spread = random.nextFloat();
        this.swirls = swirls;
    }

    @Override
    protected float getU1() {
        return super.getU1() - (super.getU1() - super.getU0())/8f;
    }

    @Override
    protected float getV1() {
        return super.getV1() - (super.getV1() - super.getV0())/8f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return MMRenderType.PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH;
    }

    @Override
    public void tick() {
        super.tick();

        if (swirls) {
            Vector3f motionVec = new Vector3f((float)xd, (float)yd, (float)zd);
            motionVec.normalize();
            float yaw = (float) Math.atan2(motionVec.x(), motionVec.z());
            float pitch = (float) Math.atan2(motionVec.y(), 1);
            float swirlRadius = 4f * (age / (float) lifetime) * spread;
            Quaternionf quatSpin = new Quaternionf(new AxisAngle4f(swirlTick * 0.2f, motionVec));
            Quaternionf quatOrient = MathUtils.quatFromRotationXYZ(pitch, yaw, 0, false);
            Vector3f vec = new Vector3f(swirlRadius, 0, 0);
            quatOrient.transform(vec);
            quatSpin.transform(vec);
            x += vec.x();
            y += vec.y();
            z += vec.z();
        }

        if (age >= lifetime) {
            remove();
        }
        age++;
        swirlTick++;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        float var = (age + partialTicks)/(float)lifetime;
        alpha = (float) (1 - Math.exp(10 * (var - 1)) - Math.pow(2000, -var));
        if (alpha < 0.01) alpha = 0.01f;
        super.render(buffer, renderInfo, partialTicks);
    }

    public static final class Provider implements ParticleProvider<DadosFlocoNeve> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(DadosFlocoNeve typeIn, @NotNull ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ParticleSnowFlake particle = new ParticleSnowFlake(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.duration(), typeIn.swirls());
            particle.pickSprite(spriteSet);
            return particle;
        }
    }

}
