package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render.entidade.camada;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render.MowzieRenderUtils;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.modelo.ferramentas.AdvancedModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CamadaCristal<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public static final ModelResourceLocation MODELO = ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "item/chefes/cristal_frostmaw"));

    private final AdvancedModelRenderer parte;

    public CamadaCristal(RenderLayerParent<T, M> renderizador, AdvancedModelRenderer parte) {
        super(renderizador);
        this.parte = parte;
    }

    @Override
    public void render(PoseStack pilha, MultiBufferSource buffer, int luz, T entidade, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!parte.showModel || parte.isHidden()) return;
        Minecraft minecraft = Minecraft.getInstance();
        pilha.pushPose();
        MowzieRenderUtils.matrixStackFromModel(pilha, parte);
        BakedModel modelo = minecraft.getModelManager().getModel(MODELO).applyTransform(ItemDisplayContext.GROUND, pilha, false);
        pilha.translate(-0.5F, -0.5F, -0.5F);
        for (RenderType tipo : modelo.getRenderTypes(ItemStack.EMPTY, true)) {
            minecraft.getItemRenderer().renderModelLists(modelo, ItemStack.EMPTY, luz, OverlayTexture.NO_OVERLAY, pilha, buffer.getBuffer(tipo));
        }
        pilha.popPose();
    }
}
