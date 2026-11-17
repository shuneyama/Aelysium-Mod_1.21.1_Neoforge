package net.aelysium.aelysiummod.conteudo.filhotes.cliente.camada;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;

public class CamadaCabecaFilhote<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {

    private final CustomHeadLayer<T, M> delegada;

    public CamadaCabecaFilhote(RenderLayerParent<T, M> pai, EntityModelSet conjunto, ItemInHandRenderer renderizadorItem, float escalaX, float escalaY, float escalaZ) {
        super(pai);
        this.delegada = new CustomHeadLayer<>(pai, conjunto, escalaX, escalaY, escalaZ, renderizadorItem);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int luz, T entidade, float limbSwing, float limbSwingAmount, float parcial, float ageInTicks, float netHeadYaw, float headPitch) {
        poseStack.pushPose();
        if (entidade.isBaby() && !(entidade instanceof Villager)) {
            poseStack.translate(0.0F, -1.0F, 0.0F);
            poseStack.scale(1.0F / 0.7F, 1.0F / 0.7F, 1.0F / 0.7F);
            poseStack.translate(0.0F, -0.03125F, 0.0F);
        }
        delegada.render(poseStack, buffer, luz, entidade, limbSwing, limbSwingAmount, parcial, ageInTicks, netHeadYaw, headPitch);
        poseStack.popPose();
    }
}
