package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public class CamadaArmaduraCavaloZumbi extends RenderLayer<AbstractHorse, HorseModel<AbstractHorse>> {

    private final HorseModel<AbstractHorse> modelo;

    public CamadaArmaduraCavaloZumbi(RenderLayerParent<AbstractHorse, HorseModel<AbstractHorse>> pai, EntityModelSet modelos) {
        super(pai);
        this.modelo = new HorseModel<>(modelos.bakeLayer(ModelLayers.HORSE_ARMOR));
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int luz, AbstractHorse cavalo, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack item = cavalo.getBodyArmorItem();
        if (!(item.getItem() instanceof AnimalArmorItem armadura) || armadura.getBodyType() != AnimalArmorItem.BodyType.EQUESTRIAN) return;
        this.getParentModel().copyPropertiesTo(this.modelo);
        this.modelo.prepareMobModel(cavalo, limbSwing, limbSwingAmount, partialTicks);
        this.modelo.setupAnim(cavalo, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        int cor = item.is(ItemTags.DYEABLE) ? FastColor.ARGB32.opaque(DyedItemColor.getOrDefault(item, -6265536)) : -1;
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityCutoutNoCull(armadura.getTexture()));
        this.modelo.renderToBuffer(pose, vertices, luz, OverlayTexture.NO_OVERLAY, cor);
    }
}
