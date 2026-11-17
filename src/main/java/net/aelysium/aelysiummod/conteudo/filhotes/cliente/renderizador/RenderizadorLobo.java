package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloLoboFilhote;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

public class RenderizadorLobo extends WolfRenderer {

    private static final ResourceLocation SELVAGEM = UtilFilhotes.textura("filhotes/wolf/wolf_baby");
    private static final ResourceLocation DOMADO = UtilFilhotes.textura("filhotes/wolf/wolf_tame_baby");
    private static final ResourceLocation BRAVO = UtilFilhotes.textura("filhotes/wolf/wolf_angry_baby");
    private static final ResourceLocation COLEIRA = UtilFilhotes.textura("filhotes/wolf/wolf_collar_baby");

    private final RenderizadorLobo filhote;

    public RenderizadorLobo(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorLobo(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto);
        if (ehFilhote) {
            this.model = new ModeloLoboFilhote(contexto.bakeLayer(CamadasFilhotes.LOBO));
            this.layers.clear();
            this.addLayer(new CamadaColeira(this));
            this.filhote = null;
        } else {
            this.filhote = new RenderizadorLobo(contexto, true);
        }
    }

    @Override
    public void render(Wolf entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
        if (filhote != null && entidade.isBaby()) {
            filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
            return;
        }
        super.render(entidade, yaw, parcial, poseStack, buffer, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(Wolf entidade) {
        ResourceLocation adulto = super.getTextureLocation(entidade);
        if (filhote != null) return adulto;
        ResourceLocation reserva = entidade.isTame() ? DOMADO : entidade.isAngry() ? BRAVO : SELVAGEM;
        return UtilFilhotes.versaoFilhote(adulto, "wolf", reserva);
    }

    private static class CamadaColeira extends RenderLayer<Wolf, WolfModel<Wolf>> {

        CamadaColeira(RenderLayerParent<Wolf, WolfModel<Wolf>> pai) {
            super(pai);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int luz, Wolf lobo, float limbSwing, float limbSwingAmount, float parcial, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!lobo.isTame() || lobo.isInvisible()) return;
            int cor = lobo.getCollarColor().getTextureDiffuseColor();
            VertexConsumer consumidor = buffer.getBuffer(RenderType.entityCutoutNoCull(COLEIRA));
            this.getParentModel().renderToBuffer(poseStack, consumidor, luz, OverlayTexture.NO_OVERLAY, cor);
        }
    }
}
