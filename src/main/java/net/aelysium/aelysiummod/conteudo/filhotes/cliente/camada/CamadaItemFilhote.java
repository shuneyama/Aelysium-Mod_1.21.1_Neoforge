package net.aelysium.aelysiummod.conteudo.filhotes.cliente.camada;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CamadaItemFilhote<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends ItemInHandLayer<T, M> {

    private final ItemInHandRenderer renderizadorItem;

    public CamadaItemFilhote(RenderLayerParent<T, M> pai, ItemInHandRenderer renderizadorItem) {
        super(pai, renderizadorItem);
        this.renderizadorItem = renderizadorItem;
    }

    @Override
    protected void renderArmWithItem(LivingEntity entidade, ItemStack item, ItemDisplayContext contexto, HumanoidArm braco, PoseStack poseStack, MultiBufferSource buffer, int luz) {
        if (item.isEmpty()) return;
        poseStack.pushPose();
        getParentModel().translateToHand(braco, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean esquerda = braco == HumanoidArm.LEFT;
        poseStack.translate(0.0F, 1.0F / 16.0F, -4.5F / 16.0F);
        renderizadorItem.renderItem(entidade, item, contexto, esquerda, poseStack, buffer, luz);
        poseStack.popPose();
    }
}
