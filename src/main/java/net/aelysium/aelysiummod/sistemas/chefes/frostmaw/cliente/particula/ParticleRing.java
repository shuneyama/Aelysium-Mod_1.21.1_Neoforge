package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.particula;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.particula.DadosAnel;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.modelo.ferramentas.MathUtils;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render.MMRenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ParticleRing extends TextureSheetParticle {
    public float r, g, b;
    public float opacity;
    public boolean facesCamera;
    public float yaw, pitch;
    public float size;

    private final DadosAnel.EnumRingBehavior behavior;

    public ParticleRing(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, float yaw, float pitch, int duration, float r, float g, float b, float opacity, float size, boolean facesCamera, DadosAnel.EnumRingBehavior behavior) {
        super(world, x, y, z);
        setSize(1, 1);
        this.size = size * 0.1f;
        lifetime = duration;
        alpha = 1;
        this.r = r;
        this.g = g;
        this.b = b;
        this.opacity = opacity;
        this.yaw = yaw;
        this.pitch = pitch;
        this.facesCamera = facesCamera;
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;
        this.behavior = behavior;
    }

    @Override
    public int getLightColor(float delta) {
        return 240 | super.getLightColor(delta) & 0xFF0000;
    }

    @Override
    public void tick() {
        super.tick();
        if (age >= lifetime) {
            remove();
        }
        age++;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        float var = (age + partialTicks)/lifetime;
        if (behavior == DadosAnel.EnumRingBehavior.GROW) {
            quadSize = size * var;
        }
        else if (behavior == DadosAnel.EnumRingBehavior.SHRINK) {
            quadSize = size * (1 - var);
        }
        else if (behavior == DadosAnel.EnumRingBehavior.GROW_THEN_SHRINK) {
            quadSize = (float) (size * (1 - var - Math.pow(2000, -var)));
        }
        else {
            quadSize = size;
        }
        alpha = opacity * 0.95f * (1 - (age + partialTicks)/lifetime) + 0.05f;
        rCol = r;
        gCol = g;
        bCol = b;

        Vec3 Vector3d = renderInfo.getPosition();
        float f = (float)(Mth.lerp(partialTicks, this.xo, this.x) - Vector3d.x());
        float f1 = (float)(Mth.lerp(partialTicks, this.yo, this.y) - Vector3d.y());
        float f2 = (float)(Mth.lerp(partialTicks, this.zo, this.z) - Vector3d.z());
        Quaternionf quaternionf = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
        if (facesCamera) {
            if (this.roll == 0.0F) {
                quaternionf = renderInfo.rotation();
            } else {
                quaternionf = new Quaternionf(renderInfo.rotation());
                float f3 = Mth.lerp(partialTicks, this.oRoll, this.roll);
                quaternionf.mul(Axis.ZP.rotation(f3));
            }
        }
        else {
            Quaternionf quatX = MathUtils.quatFromRotationXYZ(pitch, 0, 0, false);
            Quaternionf quatY = MathUtils.quatFromRotationXYZ(0, yaw, 0, false);
            quaternionf.mul(quatY);
            quaternionf.mul(quatX);
        }

        Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
        quaternionf.transform(vector3f1);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f4 = this.getQuadSize(partialTicks);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            quaternionf.transform(vector3f);
            vector3f.mul(f4);
            vector3f.add(f, f1, f2);
        }

        float f7 = this.getU0();
        float f8 = this.getU1();
        float f5 = this.getV0();
        float f6 = this.getV1();
        int j = this.getLightColor(partialTicks);
        buffer.addVertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).setUv(f8, f6).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        buffer.addVertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).setUv(f8, f5).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        buffer.addVertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).setUv(f7, f5).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        buffer.addVertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).setUv(f7, f6).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return MMRenderType.PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH;
    }

    public static final class Provider implements ParticleProvider<DadosAnel> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(DadosAnel typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ParticleRing particle = new ParticleRing(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.yaw(), typeIn.pitch(), typeIn.duration(), typeIn.red(), typeIn.green(), typeIn.blue(), typeIn.alpha(), typeIn.scale(), typeIn.facesCamera(), typeIn.behavior());
            particle.setSpriteFromAge(spriteSet);
            return particle;
        }
    }

}
