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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RenderizadorItemCigarro extends BlockEntityWithoutLevelRenderer {

    public static final RenderizadorItemCigarro INSTANCE = new RenderizadorItemCigarro();

    private static final int FUMADAS_PARA_USADO = 5;

    private static final ModelResourceLocation NOVO = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "item/cigarro_novo"));
    private static final ModelResourceLocation USADO = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "item/cigarro_usado"));

    private RenderizadorItemCigarro() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int fumadas = stack.getOrDefault(ModComponentesDados.CIGARRO_FUMADAS, 0);

        var modelManager = Minecraft.getInstance().getModelManager();
        BakedModel model = modelManager.getModel(fumadas >= FUMADAS_PARA_USADO ? USADO : NOVO);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        poseStack.pushPose();
        for (BakedModel pass : model.getRenderPasses(stack, false)) {
            for (RenderType renderType : pass.getRenderTypes(stack, false)) {
                VertexConsumer buf = buffer.getBuffer(renderType);
                itemRenderer.renderModelLists(pass, stack, packedLight, packedOverlay, poseStack, buf);
            }
        }
        poseStack.popPose();
    }
}
