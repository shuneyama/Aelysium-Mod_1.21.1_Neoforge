package net.aelysium.aelysiummod.protecao.evento;

import net.aelysium.aelysiummod.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.protecao.regiao.SelecaoManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EventHandlerMovimento {

    private static final Map<UUID, Integer> tickCounters = new ConcurrentHashMap<>();
    private static final int VERIFICACAO_INTERVALO = 10;

    private int tickSalvar = 0;
    private static final int INTERVALO_SALVAR = 1200;

    @SubscribeEvent
    public void aoTickJogador(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;

        UUID uuid = jogador.getUUID();
        int counter = tickCounters.getOrDefault(uuid, 0) + 1;

        if (counter >= VERIFICACAO_INTERVALO) {
            counter = 0;
            EventHandlerProtecao.verificarMovimentoJogador(jogador);
        }
        tickCounters.put(uuid, counter);

        tickSalvar++;
        if (tickSalvar >= INTERVALO_SALVAR) {
            tickSalvar = 0;
            GerenciadorRegioes.getInstance().salvarSeNecessario();
        }
    }

    @SubscribeEvent
    public void aoJogadorDesconectar(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer jogador) {
            EventHandlerProtecao.limparJogador(jogador.getUUID());
            SelecaoManager.limpar(jogador.getUUID());
            tickCounters.remove(jogador.getUUID());
        }
    }
}
