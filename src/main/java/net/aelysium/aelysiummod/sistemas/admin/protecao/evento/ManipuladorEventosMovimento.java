package net.aelysium.aelysiummod.sistemas.admin.protecao.evento;

import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorSelecao;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ManipuladorEventosMovimento {

    private static final Map<UUID, Integer> tickCounters = new ConcurrentHashMap<>();
    private static final int VERIFICACAO_INTERVALO = 5;

    private int tickSalvar = 0;
    private static final int INTERVALO_SALVAR = 1200;

    @SubscribeEvent
    public void aoTickJogador(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;

        UUID uuid = jogador.getUUID();
        int counter = tickCounters.getOrDefault(uuid, 0) + 1;

        if (counter >= VERIFICACAO_INTERVALO) {
            counter = 0;
            ControleRegiaoJogador.verificarMovimentoJogador(jogador);
        }
        tickCounters.put(uuid, counter);

        tickSalvar++;
        if (tickSalvar >= INTERVALO_SALVAR) {
            tickSalvar = 0;
            GerenciadorRegioes.getInstance().salvarSeNecessario();
        }
    }

    @SubscribeEvent
    public void aoRenascer(PlayerEvent.PlayerRespawnEvent event) {
        if (event.isEndConquered()) return;
        if (event.getEntity() instanceof ServerPlayer jogador) {
            ControleRegiaoJogador.liberarAposMorte(jogador);
        }
    }

    @SubscribeEvent
    public void aoJogadorDesconectar(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer jogador) {
            ControleRegiaoJogador.limparJogador(jogador.getUUID());
            GerenciadorSelecao.limpar(jogador.getUUID());
            tickCounters.remove(jogador.getUUID());
        }
    }
}
