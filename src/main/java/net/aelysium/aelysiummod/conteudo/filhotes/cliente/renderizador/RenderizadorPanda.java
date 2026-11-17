package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloPandaFilhote;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PandaRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Panda;

import java.util.EnumMap;
import java.util.Map;

public class RenderizadorPanda extends PandaRenderer {

    private static final Map<Panda.Gene, ResourceLocation> FILHOTES = new EnumMap<>(Panda.Gene.class);

    static {
        FILHOTES.put(Panda.Gene.NORMAL, UtilFilhotes.textura("filhotes/panda/panda_baby"));
        FILHOTES.put(Panda.Gene.LAZY, UtilFilhotes.textura("filhotes/panda/lazy_panda_baby"));
        FILHOTES.put(Panda.Gene.WORRIED, UtilFilhotes.textura("filhotes/panda/worried_panda_baby"));
        FILHOTES.put(Panda.Gene.PLAYFUL, UtilFilhotes.textura("filhotes/panda/playful_panda_baby"));
        FILHOTES.put(Panda.Gene.BROWN, UtilFilhotes.textura("filhotes/panda/brown_panda_baby"));
        FILHOTES.put(Panda.Gene.WEAK, UtilFilhotes.textura("filhotes/panda/weak_panda_baby"));
        FILHOTES.put(Panda.Gene.AGGRESSIVE, UtilFilhotes.textura("filhotes/panda/aggressive_panda_baby"));
    }

    private final RenderizadorPanda filhote;

    public RenderizadorPanda(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorPanda(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto);
        if (ehFilhote) {
            this.model = new ModeloPandaFilhote(contexto.bakeLayer(CamadasFilhotes.PANDA));
            this.filhote = null;
        } else {
            this.filhote = new RenderizadorPanda(contexto, true);
        }
    }

    @Override
    public void render(Panda entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
        if (filhote != null && entidade.isBaby()) {
            filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
            return;
        }
        super.render(entidade, yaw, parcial, poseStack, buffer, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(Panda entidade) {
        if (filhote != null) return super.getTextureLocation(entidade);
        return FILHOTES.getOrDefault(entidade.getVariant(), FILHOTES.get(Panda.Gene.NORMAL));
    }
}
