package net.aelysium.aelysiummod.conteudo.item.glock.cliente;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.entidade.EntidadeBalaGlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderizadorBalaGlock extends EntityRenderer<EntidadeBalaGlock> {

    private static final ResourceLocation TEXTURA =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/entity/glock_bala.png");

    private static final float METADE_COMPRIMENTO = 1.0f;
    private static final float METADE_ESPESSURA = 0.5f;

    public RenderizadorBalaGlock(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EntidadeBalaGlock entity) {
        return TEXTURA;
    }

    @Override
    public void render(EntidadeBalaGlock entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        poseStack.scale(1 / 16f, 1 / 16f, 1 / 16f);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURA));
        PoseStack.Pose pose = poseStack.last();

        float x0 = -METADE_COMPRIMENTO, x1 = METADE_COMPRIMENTO;
        float y0 = -METADE_ESPESSURA, y1 = METADE_ESPESSURA;
        float z0 = -METADE_ESPESSURA, z1 = METADE_ESPESSURA;

        quad(pose, vertexConsumer, packedLight, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, 1, 0, 0);
        quad(pose, vertexConsumer, packedLight, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, -1, 0, 0);
        quad(pose, vertexConsumer, packedLight, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, 0, 1, 0);
        quad(pose, vertexConsumer, packedLight, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, 0, -1, 0);
        quad(pose, vertexConsumer, packedLight, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1, 0, 0, 1);
        quad(pose, vertexConsumer, packedLight, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, 0, 0, -1);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void quad(PoseStack.Pose pose, VertexConsumer buffer, int light,
                       float ax, float ay, float az, float bx, float by, float bz,
                       float cx, float cy, float cz, float dx, float dy, float dz,
                       int nx, int ny, int nz) {
        vertex(pose, buffer, light, ax, ay, az, 0f, 1f, nx, ny, nz);
        vertex(pose, buffer, light, bx, by, bz, 1f, 1f, nx, ny, nz);
        vertex(pose, buffer, light, cx, cy, cz, 1f, 0f, nx, ny, nz);
        vertex(pose, buffer, light, dx, dy, dz, 0f, 0f, nx, ny, nz);
    }

    private void vertex(PoseStack.Pose pose, VertexConsumer buffer, int light,
                         float x, float y, float z, float u, float v, int nx, int ny, int nz) {
        buffer.addVertex(pose, x, y, z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}
