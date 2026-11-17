package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloTartarugaFilhote;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TurtleRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Turtle;

public class RenderizadorTartaruga extends TurtleRenderer {

    private static final ResourceLocation FILHOTE = UtilFilhotes.textura("filhotes/turtle/turtle_baby");

    private final RenderizadorTartaruga filhote;

    public RenderizadorTartaruga(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorTartaruga(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto);
        if (ehFilhote) {
            this.model = new ModeloTartarugaFilhote(contexto.bakeLayer(CamadasFilhotes.TARTARUGA));
            this.filhote = null;
        } else {
            this.filhote = new RenderizadorTartaruga(contexto, true);
        }
    }

    @Override
    public void render(Turtle entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
        if (filhote != null && entidade.isBaby()) {
            filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
            return;
        }
        super.render(entidade, yaw, parcial, poseStack, buffer, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(Turtle entidade) {
        return filhote == null ? FILHOTE : super.getTextureLocation(entidade);
    }
}
