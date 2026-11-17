package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloGatoFilhote;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;

public class RenderizadorGato extends CatRenderer {

    private static final ResourceLocation RESERVA = UtilFilhotes.textura("filhotes/cat/cat_tabby_baby");
    private static final ResourceLocation COLEIRA = UtilFilhotes.textura("filhotes/cat/cat_collar_baby");

    private final RenderizadorGato filhote;

    public RenderizadorGato(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorGato(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto);
        if (ehFilhote) {
            this.model = new ModeloGatoFilhote(contexto.bakeLayer(CamadasFilhotes.FELINO));
            this.layers.clear();
            this.addLayer(new CamadaColeira(this, new ModeloGatoFilhote(contexto.bakeLayer(CamadasFilhotes.FELINO_COLEIRA))));
            this.filhote = null;
        } else {
            this.filhote = new RenderizadorGato(contexto, true);
        }
    }

    @Override
    public void render(Cat entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
        if (filhote != null && entidade.isBaby()) {
            filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
            return;
        }
        super.render(entidade, yaw, parcial, poseStack, buffer, luz);
    }

    @Override
    protected void scale(Cat entidade, PoseStack poseStack, float parcial) {
        if (filhote != null) super.scale(entidade, poseStack, parcial);
    }

    @Override
    public ResourceLocation getTextureLocation(Cat entidade) {
        ResourceLocation adulto = super.getTextureLocation(entidade);
        if (filhote != null) return adulto;
        if (!"minecraft".equals(adulto.getNamespace())) return RESERVA;
        String caminho = adulto.getPath();
        String arquivo = caminho.substring(caminho.lastIndexOf('/') + 1).replace(".png", "");
        if (!arquivo.startsWith("cat_")) arquivo = "cat_" + arquivo;
        ResourceLocation bebe = UtilFilhotes.textura("filhotes/cat/" + arquivo + "_baby");
        return UtilFilhotes.existe(bebe) ? bebe : RESERVA;
    }

    private static class CamadaColeira extends RenderLayer<Cat, CatModel<Cat>> {

        private final ModeloGatoFilhote modelo;

        CamadaColeira(RenderLayerParent<Cat, CatModel<Cat>> pai, ModeloGatoFilhote modelo) {
            super(pai);
            this.modelo = modelo;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int luz, Cat gato, float limbSwing, float limbSwingAmount, float parcial, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!gato.isTame() || gato.isInvisible()) return;
            this.getParentModel().copyPropertiesTo(modelo);
            modelo.prepareMobModel(gato, limbSwing, limbSwingAmount, parcial);
            modelo.setupAnim(gato, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            poseStack.pushPose();
            poseStack.translate(0.0F, 1.501F, 0.0F);
            poseStack.scale(1.01F, 1.01F, 1.01F);
            poseStack.translate(0.0F, -1.501F, 0.0F);
            VertexConsumer consumidor = buffer.getBuffer(RenderType.entityCutoutNoCull(COLEIRA));
            modelo.renderToBuffer(poseStack, consumidor, luz, LivingEntityRenderer.getOverlayCoords(gato, 0.0F), gato.getCollarColor().getTextureDiffuseColor());
            poseStack.popPose();
        }
    }
}
