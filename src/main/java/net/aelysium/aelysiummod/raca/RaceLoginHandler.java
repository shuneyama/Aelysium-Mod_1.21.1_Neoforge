package net.aelysium.aelysiummod.raca;

import net.aelysium.aelysiummod.deus.FormaDivina;
import net.aelysium.aelysiummod.network.ValkyriaFlightPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class RaceLoginHandler {

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
            ValkyriaFlightManager.clearData(newPlayer.getUUID());
            RaceTicker.clearUndyneState(newPlayer.getUUID());
            RaceTicker.clearValkyriaCooldown(newPlayer.getUUID());
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
        ValkyriaFlightManager.clearData(player.getUUID());
        RaceTicker.removeFromCache(player.getUUID());
        FormaDivina.onPlayerLogout(player.getUUID());
    }

    private static void reapplyRace(ServerPlayer player) {
        RaceData data = RaceData.get(player.serverLevel());
        RaceType race = data.getRace(player.getUUID());

        RaceTicker.updateCache(player.getUUID(), race);
        RaceManager.applyRace(player, race);

        if (race != RaceType.VALKYRIA) {
            PacketDistributor.sendToPlayer(player, new ValkyriaFlightPacket(false, 0, 0));
        }
    }
}