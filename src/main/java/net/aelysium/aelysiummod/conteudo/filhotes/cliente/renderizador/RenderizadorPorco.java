package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.ClimaFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloPorcoFilhote;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;

import java.util.Map;

public class RenderizadorPorco extends PigRenderer {

    private static final Map<ClimaFilhotes, ResourceLocation> ADULTO = ClimaFilhotes.mapa(c -> c.textura("pig", "pig", false));
    private static final Map<ClimaFilhotes, ResourceLocation> FILHOTE = ClimaFilhotes.mapa(c -> c.textura("pig", "pig", true));

    private final RenderizadorPorco filhote;
    private PigModel<Pig> modeloNormal;
    private PigModel<Pig> modeloFrio;

    public RenderizadorPorco(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorPorco(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto);
        if (ehFilhote) {
            this.model = new ModeloPorcoFilhote<>(contexto.bakeLayer(CamadasFilhotes.PORCO));
            this.layers.clear();
            this.filhote = null;
        } else {
            this.modeloNormal = new PigModel<>(contexto.bakeLayer(CamadasFilhotes.PORCO_ADULTO));
            this.modeloFrio = new PigModel<>(contexto.bakeLayer(CamadasFilhotes.PORCO_FRIO));
            this.model = modeloNormal;
            this.filhote = new RenderizadorPorco(contexto, true);
        }
    }

    @Override
    public void render(Pig entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
        if (filhote != null) {
            if (entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            this.model = ClimaFilhotes.de(entidade) == ClimaFilhotes.FRIO ? modeloFrio : modeloNormal;
        }
        super.render(entidade, yaw, parcial, poseStack, buffer, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(Pig entidade) {
        ClimaFilhotes clima = ClimaFilhotes.de(entidade);
        return filhote == null ? FILHOTE.get(clima) : ADULTO.get(clima);
    }
}
