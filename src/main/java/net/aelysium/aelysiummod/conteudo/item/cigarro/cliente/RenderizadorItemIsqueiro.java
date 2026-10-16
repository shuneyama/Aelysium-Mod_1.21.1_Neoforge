package net.aelysium.aelysiummod.conteudo.item.cigarro.cliente;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RenderizadorItemIsqueiro extends BlockEntityWithoutLevelRenderer {

    public static final RenderizadorItemIsqueiro INSTANCE = new RenderizadorItemIsqueiro();

    private static final ModelResourceLocation CORPO_APAGADO = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "item/isqueiro_corpo_apagado"));
    private static final ModelResourceLocation CORPO_ACESO = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "item/isqueiro_corpo_aceso"));
    private static final ModelResourceLocation TAMPA = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "item/isqueiro_tampa"));

    private static final float ANGULO_ABERTO = -45f;
    private static final float ANGULO_FECHADO = 90f;
    private static final double HINGE_X = 9.5 / 16.0;
    private static final double HINGE_Y = 4.0 / 16.0;
    private static final double HINGE_Z = 7.0 / 16.0;

    private RenderizadorItemIsqueiro() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean aceso = stack.getOrDefault(ModComponentesDados.ISQUEIRO_ACESO, false);
        boolean tampaAberta = stack.getOrDefault(ModComponentesDados.ISQUEIRO_TAMPA_ABERTA, false);
        long tampaTick = stack.getOrDefault(ModComponentesDados.ISQUEIRO_TAMPA_TICK, 0L);

        var modelManager = Minecraft.getInstance().getModelManager();
        BakedModel corpoModel = modelManager.getModel(aceso ? CORPO_ACESO : CORPO_APAGADO);
        BakedModel tampaModel = modelManager.getModel(TAMPA);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        long gameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L;
        float t = Mth.clamp((gameTime - tampaTick) / 20f, 0f, 1f);
        float angulo = tampaAberta ? Mth.lerp(t, ANGULO_FECHADO, ANGULO_ABERTO) : Mth.lerp(t, ANGULO_ABERTO, ANGULO_FECHADO);

        poseStack.pushPose();

        drawModel(itemRenderer, corpoModel, stack, poseStack, buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(HINGE_X, HINGE_Y, HINGE_Z);
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angulo));
        poseStack.translate(-HINGE_X, -HINGE_Y, -HINGE_Z);
        drawModel(itemRenderer, tampaModel, stack, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void drawModel(ItemRenderer itemRenderer, BakedModel model, ItemStack stack, PoseStack poseStack,
                                   MultiBufferSource buffer, int packedLight, int packedOverlay) {
        for (BakedModel pass : model.getRenderPasses(stack, false)) {
            for (RenderType renderType : pass.getRenderTypes(stack, false)) {
                VertexConsumer buf = buffer.getBuffer(renderType);
                itemRenderer.renderModelLists(pass, stack, packedLight, packedOverlay, poseStack, buf);
            }
        }
    }
}
