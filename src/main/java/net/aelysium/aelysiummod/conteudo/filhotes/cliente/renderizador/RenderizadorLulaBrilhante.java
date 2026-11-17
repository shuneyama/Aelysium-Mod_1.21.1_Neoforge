package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.conteudo.filhotes.aquatico.IdadeAquatica;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GlowSquidRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.GlowSquid;

public class RenderizadorLulaBrilhante extends GlowSquidRenderer {

    private static final ResourceLocation FILHOTE = UtilFilhotes.textura("filhotes/squid/glow_squid_baby");

    private final RenderizadorLulaBrilhante filhote;

    public RenderizadorLulaBrilhante(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorLulaBrilhante(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto, new SquidModel<>(contexto.bakeLayer(ehFilhote ? CamadasFilhotes.LULA : ModelLayers.GLOW_SQUID)));
        this.filhote = ehFilhote ? null : new RenderizadorLulaBrilhante(contexto, true);
    }

    @Override
    public void render(GlowSquid entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
        if (filhote != null && IdadeAquatica.ehFilhote(entidade)) {
            float inverso = 1.0F / entidade.getScale();
            poseStack.pushPose();
            poseStack.scale(inverso, inverso, inverso);
            filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
            poseStack.popPose();
            return;
        }
        super.render(entidade, yaw, parcial, poseStack, buffer, luz);
    }

    @Override
    protected void setupRotations(GlowSquid entidade, PoseStack poseStack, float bob, float yBodyRot, float parcial, float escala) {
        if (filhote == null) {
            RotacaoLulaFilhote.aplicar(entidade, poseStack, yBodyRot, parcial);
            return;
        }
        super.setupRotations(entidade, poseStack, bob, yBodyRot, parcial, escala);
    }

    @Override
    public ResourceLocation getTextureLocation(GlowSquid entidade) {
        return filhote == null ? FILHOTE : super.getTextureLocation(entidade);
    }
}
