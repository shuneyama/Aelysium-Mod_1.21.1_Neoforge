package net.aelysium.aelysiummod.sistemas.mundo.jardimpalido.cliente;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.mundo.jardimpalido.Creaking;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RenderizadorCreaking extends MobRenderer<Creaking, ModeloCreaking> {

    public static final ModelLayerLocation CAMADA = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "creaking"), "main");
    private static final ResourceLocation TEXTURA = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/entity/creaking/creaking.png");
    private static final ResourceLocation TEXTURA_OLHOS = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/entity/creaking/creaking_eyes.png");

    public RenderizadorCreaking(EntityRendererProvider.Context contexto) {
        super(contexto, new ModeloCreaking(contexto.bakeLayer(CAMADA)), 0.7F);
        this.addLayer(new Olhos(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Creaking entidade) {
        return TEXTURA;
    }

    private static class Olhos extends RenderLayer<Creaking, ModeloCreaking> {
        Olhos(RenderLayerParent<Creaking, ModeloCreaking> pai) {
            super(pai);
        }

        @Override
        public void render(PoseStack pilha, MultiBufferSource buffer, int luz, Creaking entidade, float balanco, float velocidade,
                           float parcial, float idade, float guinada, float arfagem) {
            if (!entidade.podeMover() || entidade.isInvisible()) return;
            VertexConsumer vertices = buffer.getBuffer(RenderType.eyes(TEXTURA_OLHOS));
            ModeloCreaking modelo = this.getParentModel();
            modelo.somenteCabeca(true);
            modelo.renderToBuffer(pilha, vertices, 0xF000F0, OverlayTexture.NO_OVERLAY, -1);
            modelo.somenteCabeca(false);
        }
    }
}
