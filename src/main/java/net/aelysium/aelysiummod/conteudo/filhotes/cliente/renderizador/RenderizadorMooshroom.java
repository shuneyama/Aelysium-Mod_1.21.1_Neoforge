package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloVacaFilhote;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MushroomCowRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.MushroomCow;

public class RenderizadorMooshroom extends MushroomCowRenderer {

    private static final ResourceLocation ADULTO_VERMELHO = UtilFilhotes.textura("adultos/cow/mooshroom_red");
    private static final ResourceLocation ADULTO_MARROM = UtilFilhotes.textura("adultos/cow/mooshroom_brown");
    private static final ResourceLocation FILHOTE_VERMELHO = UtilFilhotes.textura("filhotes/cow/mooshroom_red_baby");
    private static final ResourceLocation FILHOTE_MARROM = UtilFilhotes.textura("filhotes/cow/mooshroom_brown_baby");

    private final RenderizadorMooshroom filhote;

    public RenderizadorMooshroom(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorMooshroom(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto);
        if (ehFilhote) {
            this.model = new ModeloVacaFilhote<>(contexto.bakeLayer(CamadasFilhotes.VACA));
            this.layers.clear();
            this.filhote = null;
        } else {
            this.model = new CowModel<>(contexto.bakeLayer(CamadasFilhotes.VACA_ADULTA));
            this.filhote = new RenderizadorMooshroom(contexto, true);
        }
    }

    @Override
    public void render(MushroomCow entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
        if (filhote != null && entidade.isBaby()) {
            filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
            return;
        }
        super.render(entidade, yaw, parcial, poseStack, buffer, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(MushroomCow entidade) {
        boolean marrom = entidade.getVariant() == MushroomCow.MushroomType.BROWN;
        if (filhote == null) return marrom ? FILHOTE_MARROM : FILHOTE_VERMELHO;
        return marrom ? ADULTO_MARROM : ADULTO_VERMELHO;
    }
}
