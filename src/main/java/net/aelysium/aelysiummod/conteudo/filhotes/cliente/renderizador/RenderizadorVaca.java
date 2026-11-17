package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.ClimaFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloVacaFilhote;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;

import java.util.EnumMap;
import java.util.Map;

public class RenderizadorVaca extends CowRenderer {

    private static final Map<ClimaFilhotes, ResourceLocation> ADULTO = ClimaFilhotes.mapa(c -> c.textura("cow", "cow", false));
    private static final Map<ClimaFilhotes, ResourceLocation> FILHOTE = ClimaFilhotes.mapa(c -> c.textura("cow", "cow", true));

    private final RenderizadorVaca filhote;
    private final Map<ClimaFilhotes, CowModel<Cow>> modelos = new EnumMap<>(ClimaFilhotes.class);

    public RenderizadorVaca(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorVaca(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto);
        if (ehFilhote) {
            this.model = new ModeloVacaFilhote<>(contexto.bakeLayer(CamadasFilhotes.VACA));
            this.filhote = null;
        } else {
            modelos.put(ClimaFilhotes.TEMPERADO, new CowModel<>(contexto.bakeLayer(CamadasFilhotes.VACA_ADULTA)));
            modelos.put(ClimaFilhotes.QUENTE, new CowModel<>(contexto.bakeLayer(CamadasFilhotes.VACA_QUENTE)));
            modelos.put(ClimaFilhotes.FRIO, new CowModel<>(contexto.bakeLayer(CamadasFilhotes.VACA_FRIA)));
            this.model = modelos.get(ClimaFilhotes.TEMPERADO);
            this.filhote = new RenderizadorVaca(contexto, true);
        }
    }

    @Override
    public void render(Cow entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
        if (filhote != null) {
            if (entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            this.model = modelos.get(ClimaFilhotes.de(entidade));
        }
        super.render(entidade, yaw, parcial, poseStack, buffer, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(Cow entidade) {
        ClimaFilhotes clima = ClimaFilhotes.de(entidade);
        return filhote == null ? FILHOTE.get(clima) : ADULTO.get(clima);
    }
}
