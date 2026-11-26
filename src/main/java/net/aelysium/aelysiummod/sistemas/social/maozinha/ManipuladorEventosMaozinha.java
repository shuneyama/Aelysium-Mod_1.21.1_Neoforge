package net.aelysium.aelysiummod.sistemas.social.maozinha;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

public class ManipuladorEventosMaozinha {

    @SubscribeEvent
    public void aoEntrar(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer jogador) {
            GerenciadorMaozinha.sincronizar(jogador);
        }
    }

    @SubscribeEvent
    public void aoSair(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer jogador) {
            GerenciadorMaozinha.limparJogador(jogador, GerenciadorMaozinha.Motivo.DESCONEXAO);
        }
    }

    @SubscribeEvent
    public void aoMorrer(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer jogador) {
            GerenciadorMaozinha.limparJogador(jogador, GerenciadorMaozinha.Motivo.ESTADO);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void aoTeleportarPorComando(EntityTeleportEvent.TeleportCommand event) {
        if (event.getEntity() instanceof ServerPlayer jogador) {
            GerenciadorMaozinha.marcarTeleporteComando(jogador);
        }
    }

    @SubscribeEvent
    public void aoPararServidor(ServerStoppedEvent event) {
        GerenciadorMaozinha.limparTudo();
    }
}
