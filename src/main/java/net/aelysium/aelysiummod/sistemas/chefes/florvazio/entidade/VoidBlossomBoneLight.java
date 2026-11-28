package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.OssoModelo;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.IBoneLight;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.IRenderer;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.MathUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector4f;

public class VoidBlossomBoneLight implements IBoneLight, IRenderer<VoidBlossomEntity> {
    private VoidBlossomEntity entity;
    private float partialTicks;

    @Override
    public int getLightForBone(OssoModelo bone, int packedLight) {
        if (bone.getName().contains("Spike") || bone.getName().contains("Thorn") || bone.getName().contains("FlowerCenter"))
            return IBoneLight.fullbright;
        else
            return packedLight;
    }

    @Override
    public Vector4f getColorForBone(OssoModelo bone, Vector4f rgbaColor) {
        if (entity == null)
            return rgbaColor;

        if (entity.isDeadOrDying()){
            float interceptedTime = MathUtils.ratioLerp(entity.deathTime, 0.5f, LightBlockRemover.deathMaxAge, partialTicks);
            rgbaColor.mul(1f - interceptedTime * 0.5f);
        }

        return rgbaColor;
    }

    @Override
    public void render(VoidBlossomEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        this.entity = entity;
        this.partialTicks = partialTicks;
    }
}
