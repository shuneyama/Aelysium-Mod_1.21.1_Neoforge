package net.aelysium.aelysiummod.conteudo.filhotes.cliente.camada;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerDataHolder;

public class CamadaTipoAldeaoFilhote<T extends LivingEntity & VillagerDataHolder, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private final String pasta;

    public CamadaTipoAldeaoFilhote(RenderLayerParent<T, M> pai, String pasta) {
        super(pai);
        this.pasta = pasta;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int luz, T entidade, float limbSwing, float limbSwingAmount, float parcial, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entidade.isInvisible()) return;
        ResourceLocation tipo = BuiltInRegistries.VILLAGER_TYPE.getKey(entidade.getVillagerData().getType());
        if (!"minecraft".equals(tipo.getNamespace())) return;
        ResourceLocation textura = UtilFilhotes.textura("filhotes/" + pasta + "/baby/" + tipo.getPath());
        if (!UtilFilhotes.existe(textura)) return;
        renderColoredCutoutModel(getParentModel(), textura, poseStack, buffer, luz, entidade, -1);
    }
}
