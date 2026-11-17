package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloJaguatiricaFilhote;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.OcelotRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Ocelot;

public class RenderizadorJaguatirica extends OcelotRenderer {

    private static final ResourceLocation FILHOTE = UtilFilhotes.textura("filhotes/cat/ocelot_baby");

    private final RenderizadorJaguatirica filhote;

    public RenderizadorJaguatirica(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorJaguatirica(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto);
        if (ehFilhote) {
            this.model = new ModeloJaguatiricaFilhote(contexto.bakeLayer(CamadasFilhotes.FELINO));
            this.filhote = null;
        } else {
            this.filhote = new RenderizadorJaguatirica(contexto, true);
        }
    }

    @Override
    public void render(Ocelot entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
        if (filhote != null && entidade.isBaby()) {
            filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
            return;
        }
        super.render(entidade, yaw, parcial, poseStack, buffer, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(Ocelot entidade) {
        return filhote == null ? FILHOTE : super.getTextureLocation(entidade);
    }
}
