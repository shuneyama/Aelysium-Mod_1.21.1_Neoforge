package net.aelysium.aelysiummod.sistemas.rp.raca;

import net.aelysium.aelysiummod.sistemas.rp.raca.rede.PacoteVooValkyria;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;

public class ManipuladorLoginRaca {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        reapplyRace(player);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;
        reapplyRace(newPlayer);

        if (event.isWasDeath()) {
            GerenciadorVooValkyria.clearData(newPlayer.getUUID());
            TemporizadorRaca.clearUndyneState(newPlayer.getUUID());
            TemporizadorRaca.clearValkyriaCooldown(newPlayer.getUUID());
        }
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        reapplyRace(player);
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        GerenciadorVooValkyria.clearData(player.getUUID());
        TemporizadorRaca.removeFromCache(player.getUUID());
    }

    private static void reapplyRace(ServerPlayer player) {
        DadosRaca data = DadosRaca.get(player.serverLevel());
        TipoRaca race = data.getRace(player.getUUID());

        TemporizadorRaca.updateCache(player.getUUID(), race);
        GerenciadorRaca.applyRace(player, race);

        if (race != TipoRaca.VALKYRIA) {
            PacketDistributor.sendToPlayer(player, new PacoteVooValkyria(false, 0, 0));
        }
    }
}