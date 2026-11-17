package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.conteudo.filhotes.aquatico.IdadeAquatica;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.DolphinRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Dolphin;

public class RenderizadorGolfinho extends DolphinRenderer {

    private static final ResourceLocation FILHOTE = UtilFilhotes.textura("filhotes/dolphin/dolphin_baby");

    private final RenderizadorGolfinho filhote;

    public RenderizadorGolfinho(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorGolfinho(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto);
        if (ehFilhote) {
            this.model = new DolphinModel<>(contexto.bakeLayer(CamadasFilhotes.GOLFINHO));
            this.filhote = null;
        } else {
            this.filhote = new RenderizadorGolfinho(contexto, true);
        }
    }

    @Override
    public void render(Dolphin entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
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
    public ResourceLocation getTextureLocation(Dolphin entidade) {
        return filhote == null ? FILHOTE : super.getTextureLocation(entidade);
    }
}
