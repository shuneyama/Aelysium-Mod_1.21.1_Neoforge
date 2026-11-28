package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ComumFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.modelo.ModelFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render.MowzieRenderUtils;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render.entidade.camada.CamadaCristal;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.EntityFrostmaw;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class RenderFrostmaw extends MobRenderer<EntityFrostmaw, ModelFrostmaw<EntityFrostmaw>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ComumFrostmaw.MODID, "textures/entity/chefes/frostmaw.png");

    public RenderFrostmaw(EntityRendererProvider.Context mgr) {
        super(mgr, new ModelFrostmaw<>(), 3.5f);
        addLayer(new CamadaCristal<>(this, getModel().iceCrystalHand));
        addLayer(new CamadaCristal<>(this, getModel().iceCrystal));
    }

    @Override
    protected float getFlipDegrees(EntityFrostmaw entity) {
        return 0;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFrostmaw entity) {
        return RenderFrostmaw.TEXTURE;
    }

    @Override
    public void render(EntityFrostmaw entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        if (entity.getAnimation() == EntityFrostmaw.SWIPE_ANIMATION || entity.getAnimation() == EntityFrostmaw.SWIPE_TWICE_ANIMATION || entity.getAnimation() == EntityFrostmaw.ICE_BREATH_ANIMATION || entity.getAnimation() == EntityFrostmaw.ICE_BALL_ANIMATION || !entity.getActive()) {
            Vec3 rightHandPos = MowzieRenderUtils.getWorldPosFromModel(entity, entityYaw, model.rightHandSocket);
            Vec3 leftHandPos = MowzieRenderUtils.getWorldPosFromModel(entity, entityYaw, model.leftHandSocket);
            Vec3 mouthPos = MowzieRenderUtils.getWorldPosFromModel(entity, entityYaw, model.mouthSocket);
            entity.setSocketPosArray(0, rightHandPos);
            entity.setSocketPosArray(1, leftHandPos);
            entity.setSocketPosArray(2, mouthPos);
        }
    }
}
