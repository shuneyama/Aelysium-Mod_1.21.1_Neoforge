package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ComumFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.modelo.ModelIceBall;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.efeitos.EntityIceBall;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RenderIceBall extends EntityRenderer<EntityIceBall> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ComumFrostmaw.MODID, "textures/entity/chefes/bola_de_gelo.png");
    public ModelIceBall model;

    public RenderIceBall(EntityRendererProvider.Context mgr) {
        super(mgr);
        model = new ModelIceBall();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityIceBall entity) {
        return TEXTURE;
    }

    @Override
    public void render(EntityIceBall entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.YN.rotation(entityYaw * (float) Math.PI / 180f));
        matrixStackIn.translate(0, -0.5f, 0);
        VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.entityTranslucent(TEXTURE));
        model.setupAnim(entityIn, 0, 0, entityIn.tickCount + partialTicks, 0, 0);
        model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
        matrixStackIn.popPose();
    }
}
