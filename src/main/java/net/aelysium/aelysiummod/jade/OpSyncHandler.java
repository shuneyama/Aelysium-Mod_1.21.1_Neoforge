package net.aelysium.aelysiummod.jade;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = "aelysiummod", bus = EventBusSubscriber.Bus.GAME)
public class OpSyncHandler {

    // Contador de ticks para sincronização periódica (100 ticks = 5 segundos)
    private static int tickCounter = 0;
    private static final int SYNC_INTERVAL = 100; // 5 segundos em ticks

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Delay de 1 tick para garantir que o jogador está completamente conectado
            player.getServer().execute(() -> syncOpPlayers(player));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        tickCounter++;
        if (tickCounter >= SYNC_INTERVAL) {
            tickCounter = 0;
            syncAllPlayers(server);
        }
    }

    private static void syncOpPlayers(ServerPlayer player) {
        if (player.getServer() == null) return;

        Set<UUID> opPlayers = new HashSet<>();

        for (ServerPlayer serverPlayer : player.getServer().getPlayerList().getPlayers()) {
            if (serverPlayer.hasPermissions(2)) {
                opPlayers.add(serverPlayer.getUUID());
            }
        }

        OpPlayersSyncPacket packet = new OpPlayersSyncPacket(opPlayers);
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void syncAllPlayers(MinecraftServer server) {
        if (server == null || server.getPlayerList() == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncOpPlayers(player);
        }
    }
}