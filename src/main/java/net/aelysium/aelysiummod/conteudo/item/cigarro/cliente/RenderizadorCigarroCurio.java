package net.aelysium.aelysiummod.conteudo.item.cigarro.cliente;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class RenderizadorCigarroCurio implements ICurioRenderer {

    private static final int FUMADAS_PARA_USADO = 5;

    private static final ResourceLocation TEXTURE_NOVO =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/item/cigarro_curios_novo.png");
    private static final ResourceLocation TEXTURE_USADO =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/item/cigarro_curios_usado.png");

    private static final float MOUTH_OFFSET_X = 0.0F;
    private static final float MOUTH_OFFSET_Y = -0.5F;
    private static final float MOUTH_OFFSET_Z = -4.2F;
    private static final float MOUTH_ROTATION_X = -90.0F;

    private static final float SCALE_X = 0.25F;
    private static final float SCALE_Y = 0.375F;
    private static final float SCALE_Z = 0.25F;

    private final ModeloCigarro model;

    public RenderizadorCigarroCurio() {
        this.model = new ModeloCigarro(Minecraft.getInstance().getEntityModels().bakeLayer(ModeloCigarro.LAYER_LOCATION));
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack,
                                                                          SlotContext slotContext,
                                                                          PoseStack poseStack,
                                                                          RenderLayerParent<T, M> renderLayerParent,
                                                                          MultiBufferSource renderTypeBuffer,
                                                                          int light, float limbSwing,
                                                                          float limbSwingAmount,
                                                                          float partialTicks,
                                                                          float ageInTicks, float netHeadYaw,
                                                                          float headPitch) {
        M entityModel = renderLayerParent.getModel();

        if (!(entityModel instanceof HumanoidModel<T> humanoidModel)) {
            return;
        }

        poseStack.pushPose();

        ModelPart head = humanoidModel.head;
        head.translateAndRotate(poseStack);

        poseStack.translate(MOUTH_OFFSET_X / 16.0F, MOUTH_OFFSET_Y / 16.0F, MOUTH_OFFSET_Z / 16.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(MOUTH_ROTATION_X));
        poseStack.scale(SCALE_X, SCALE_Y, SCALE_Z);

        int fumadas = stack.getOrDefault(ModComponentesDados.CIGARRO_FUMADAS, 0);
        ResourceLocation textura = fumadas >= FUMADAS_PARA_USADO ? TEXTURE_USADO : TEXTURE_NOVO;

        VertexConsumer vertexConsumer = renderTypeBuffer.getBuffer(RenderType.entityCutoutNoCull(textura));
        model.getBone().render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }
}
