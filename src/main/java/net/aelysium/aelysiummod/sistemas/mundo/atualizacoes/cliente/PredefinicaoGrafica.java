package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Options;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.neoforged.fml.ModList;

import java.util.Objects;

public enum PredefinicaoGrafica {
    RAPIDO("options.graphics.fast"),
    DETALHADO("options.graphics.fancy"),
    FABULOSO("options.graphics.fabulous"),
    PERSONALIZADO("options.graphics.custom");

    private static final boolean SODIUM = ModList.get() != null && ModList.get().isLoaded("sodium");

    private final String chave;

    PredefinicaoGrafica(String chave) {
        this.chave = chave;
    }

    public String chave() {
        return this.chave;
    }

    public static boolean sodium() {
        return SODIUM;
    }

    public static PredefinicaoGrafica detectar(Options o) {
        for (PredefinicaoGrafica p : new PredefinicaoGrafica[]{RAPIDO, DETALHADO, FABULOSO}) {
            if (p.corresponde(o)) return p;
        }
        return PERSONALIZADO;
    }

    private boolean corresponde(Options o) {
        boolean r = this == RAPIDO;
        boolean f = this == FABULOSO;
        int distancia = r ? 6 : this == DETALHADO ? 8 : 10;
        return o.graphicsMode().get() == grafico()
                && o.biomeBlendRadius().get() == (r ? 1 : 2)
                && o.renderDistance().get() == distancia
                && o.prioritizeChunkUpdates().get() == (r ? PrioritizeChunkUpdates.NONE : PrioritizeChunkUpdates.PLAYER_AFFECTED)
                && o.simulationDistance().get() == 5
                && o.ambientOcclusion().get() == !r
                && o.cloudStatus().get() == (r ? CloudStatus.FAST : CloudStatus.FANCY)
                && o.particles().get() == (f ? ParticleStatus.ALL : ParticleStatus.DECREASED)
                && o.mipmapLevels().get() == (r ? 2 : 4)
                && o.entityShadows().get() == !r
                && Math.abs(o.entityDistanceScaling().get() - (r ? 0.75 : f ? 1.25 : 1.0)) < 0.001
                && (SODIUM || OpcoesAtualizadas.FOLHAS_TRANSPARENTES.get() == !r)
                && OpcoesAtualizadas.RAIO_CLIMA.get() == (r ? 3 : f ? 7 : 5);
    }

    private GraphicsStatus grafico() {
        return this == RAPIDO ? GraphicsStatus.FAST : this == DETALHADO ? GraphicsStatus.FANCY : GraphicsStatus.FABULOUS;
    }

    public void aplicar(Options o) {
        if (this == PERSONALIZADO) return;
        boolean r = this == RAPIDO;
        boolean f = this == FABULOSO;
        definir(o.graphicsMode(), grafico());
        definir(o.biomeBlendRadius(), r ? 1 : 2);
        definir(o.renderDistance(), r ? 6 : this == DETALHADO ? 8 : 10);
        definir(o.prioritizeChunkUpdates(), r ? PrioritizeChunkUpdates.NONE : PrioritizeChunkUpdates.PLAYER_AFFECTED);
        definir(o.simulationDistance(), 5);
        definir(o.ambientOcclusion(), !r);
        definir(o.cloudStatus(), r ? CloudStatus.FAST : CloudStatus.FANCY);
        definir(o.particles(), f ? ParticleStatus.ALL : ParticleStatus.DECREASED);
        definir(o.mipmapLevels(), r ? 2 : 4);
        definir(o.entityShadows(), !r);
        definir(o.entityDistanceScaling(), r ? 0.75 : f ? 1.25 : 1.0);
        boolean folhas = !r;
        boolean recarregar = !SODIUM && OpcoesAtualizadas.FOLHAS_TRANSPARENTES.get() != folhas;
        if (!SODIUM) OpcoesAtualizadas.FOLHAS_TRANSPARENTES.set(folhas);
        OpcoesAtualizadas.RAIO_CLIMA.set(r ? 3 : f ? 7 : 5);
        OpcoesAtualizadas.SPEC.save();
        if (recarregar) FiltroTexturas.recarregarChunks();
    }

    private static <T> void definir(OptionInstance<T> opcao, T valor) {
        if (!Objects.equals(opcao.get(), valor)) opcao.set(valor);
    }
}
