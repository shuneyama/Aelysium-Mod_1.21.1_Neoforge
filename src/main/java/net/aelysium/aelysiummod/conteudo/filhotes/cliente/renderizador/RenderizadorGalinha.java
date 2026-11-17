package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.ClimaFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloGalinhaFilhote;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Chicken;

import java.util.Map;

public class RenderizadorGalinha extends ChickenRenderer {

    private static final Map<ClimaFilhotes, ResourceLocation> ADULTO = ClimaFilhotes.mapa(c -> c.textura("chicken", "chicken", false));
    private static final Map<ClimaFilhotes, ResourceLocation> FILHOTE = ClimaFilhotes.mapa(c -> c.textura("chicken", "chicken", true));

    private final RenderizadorGalinha filhote;
    private ChickenModel<Chicken> modeloNormal;
    private ChickenModel<Chicken> modeloFrio;

    public RenderizadorGalinha(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorGalinha(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto);
        if (ehFilhote) {
            this.model = new ModeloGalinhaFilhote<>(contexto.bakeLayer(CamadasFilhotes.GALINHA));
            this.filhote = null;
        } else {
            this.modeloNormal = new ChickenModel<>(contexto.bakeLayer(ModelLayers.CHICKEN));
            this.modeloFrio = new ChickenModel<>(contexto.bakeLayer(CamadasFilhotes.GALINHA_FRIA));
            this.model = modeloNormal;
            this.filhote = new RenderizadorGalinha(contexto, true);
        }
    }

    @Override
    public void render(Chicken entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
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
    public ResourceLocation getTextureLocation(Chicken entidade) {
        ClimaFilhotes clima = ClimaFilhotes.de(entidade);
        return filhote == null ? FILHOTE.get(clima) : ADULTO.get(clima);
    }
}
