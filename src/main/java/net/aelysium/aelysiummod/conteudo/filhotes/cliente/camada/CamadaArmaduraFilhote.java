package net.aelysium.aelysiummod.conteudo.filhotes.cliente.camada;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloArmaduraFilhote;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CamadaArmaduraFilhote<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

    private static final String PREFIXO = "textures/models/armor/";
    private static final EquipmentSlot[] ORDEM = {EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD};

    private final Map<EquipmentSlot, ModeloArmaduraFilhote<T>> modelos = new EnumMap<>(EquipmentSlot.class);
    private final float desvioBraco;

    public CamadaArmaduraFilhote(RenderLayerParent<T, M> pai, EntityModelSet conjunto, Map<EquipmentSlot, ModelLayerLocation> camadas, float desvioBraco) {
        super(pai);
        this.desvioBraco = desvioBraco;
        for (EquipmentSlot parte : ORDEM) {
            modelos.put(parte, new ModeloArmaduraFilhote<>(conjunto.bakeLayer(camadas.get(parte))));
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int luz, T entidade, float limbSwing, float limbSwingAmount, float parcial, float ageInTicks, float netHeadYaw, float headPitch) {
        for (EquipmentSlot parte : ORDEM) {
            renderizarPeca(poseStack, buffer, luz, entidade, parte);
        }
    }

    private void renderizarPeca(PoseStack poseStack, MultiBufferSource buffer, int luz, T entidade, EquipmentSlot parte) {
        ItemStack item = entidade.getItemBySlot(parte);
        if (!(item.getItem() instanceof ArmorItem armadura) || armadura.getEquipmentSlot() != parte) return;
        ModeloArmaduraFilhote<T> modelo = modelos.get(parte);
        modelo.sincronizar(getParentModel(), desvioBraco);
        ArmorMaterial material = armadura.getMaterial().value();
        IClientItemExtensions extensoes = IClientItemExtensions.of(item);
        int corPadrao = extensoes.getDefaultDyeColor(item);
        List<ArmorMaterial.Layer> camadas = material.layers();
        for (int i = 0; i < camadas.size(); i++) {
            ArmorMaterial.Layer camada = camadas.get(i);
            int cor = extensoes.getArmorLayerTintColor(item, entidade, camada, i, corPadrao);
            if (cor == 0) continue;
            ResourceLocation textura = textura(camada);
            if (textura == null) continue;
            VertexConsumer vertice = buffer.getBuffer(RenderType.armorCutoutNoCull(textura));
            modelo.renderToBuffer(poseStack, vertice, luz, OverlayTexture.NO_OVERLAY, cor);
        }
        if (item.hasFoil()) {
            modelo.renderToBuffer(poseStack, buffer.getBuffer(RenderType.armorEntityGlint()), luz, OverlayTexture.NO_OVERLAY);
        }
    }

    private static ResourceLocation textura(ArmorMaterial.Layer camada) {
        ResourceLocation original = camada.texture(false);
        if (!"minecraft".equals(original.getNamespace())) return null;
        String caminho = original.getPath();
        if (!caminho.startsWith(PREFIXO) || !caminho.endsWith(".png")) return null;
        String nome = caminho.substring(PREFIXO.length(), caminho.length() - 4).replace("_layer_1", "");
        if (nome.equals("turtle")) nome = "turtle_scute";
        ResourceLocation alvo = UtilFilhotes.textura("filhotes/humanoid_baby/" + nome);
        return UtilFilhotes.existe(alvo) ? alvo : null;
    }
}
