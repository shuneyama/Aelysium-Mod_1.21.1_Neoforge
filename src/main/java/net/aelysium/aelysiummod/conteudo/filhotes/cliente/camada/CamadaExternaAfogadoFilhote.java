package net.aelysium.aelysiummod.conteudo.filhotes.cliente.camada;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasMonstrosFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloAfogadoFilhote;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Drowned;

public class CamadaExternaAfogadoFilhote<T extends Drowned> extends RenderLayer<T, DrownedModel<T>> {

    private static final ResourceLocation TEXTURA = UtilFilhotes.textura("filhotes/zombie/drowned_outer_layer_baby");

    private final DrownedModel<T> modelo;

    public CamadaExternaAfogadoFilhote(RenderLayerParent<T, DrownedModel<T>> pai, EntityModelSet conjunto) {
        super(pai);
        this.modelo = new ModeloAfogadoFilhote<>(conjunto.bakeLayer(CamadasMonstrosFilhotes.ZUMBI_EXTERNA));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int luz, T entidade, float limbSwing, float limbSwingAmount, float parcial, float ageInTicks, float netHeadYaw, float headPitch) {
        coloredCutoutModelCopyLayerRender(getParentModel(), modelo, TEXTURA, poseStack, buffer, luz, entidade, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, parcial, -1);
    }
}
