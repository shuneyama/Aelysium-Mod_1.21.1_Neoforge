package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloCabraFilhote;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GoatRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.goat.Goat;

public class RenderizadorCabra extends GoatRenderer {

    private static final ResourceLocation FILHOTE = UtilFilhotes.textura("filhotes/goat/goat_baby");

    private final RenderizadorCabra filhote;

    public RenderizadorCabra(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorCabra(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto);
        if (ehFilhote) {
            this.model = new ModeloCabraFilhote(contexto.bakeLayer(CamadasFilhotes.CABRA));
            this.filhote = null;
        } else {
            this.filhote = new RenderizadorCabra(contexto, true);
        }
    }

    @Override
    public void render(Goat entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
        if (filhote != null && entidade.isBaby()) {
            filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
            return;
        }
        super.render(entidade, yaw, parcial, poseStack, buffer, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(Goat entidade) {
        return filhote == null ? FILHOTE : super.getTextureLocation(entidade);
    }
}
