package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloUrsoPolarFilhote;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PolarBearRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.PolarBear;

public class RenderizadorUrsoPolar extends PolarBearRenderer {

    private static final ResourceLocation FILHOTE = UtilFilhotes.textura("filhotes/bear/polarbear_baby");

    private final RenderizadorUrsoPolar filhote;

    public RenderizadorUrsoPolar(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorUrsoPolar(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto);
        if (ehFilhote) {
            this.model = new ModeloUrsoPolarFilhote(contexto.bakeLayer(CamadasFilhotes.URSO_POLAR));
            this.filhote = null;
        } else {
            this.filhote = new RenderizadorUrsoPolar(contexto, true);
        }
    }

    @Override
    public void render(PolarBear entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
        if (filhote != null && entidade.isBaby()) {
            filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
            return;
        }
        super.render(entidade, yaw, parcial, poseStack, buffer, luz);
    }

    @Override
    protected void scale(PolarBear entidade, PoseStack poseStack, float parcial) {
        if (filhote != null) super.scale(entidade, poseStack, parcial);
    }

    @Override
    public ResourceLocation getTextureLocation(PolarBear entidade) {
        return filhote == null ? FILHOTE : super.getTextureLocation(entidade);
    }
}
