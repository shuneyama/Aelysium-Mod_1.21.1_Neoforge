package net.aelysium.aelysiummod.jade;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Gerencia a lista de jogadores OP no lado do cliente.
 * Usado para ocultar informações no Jade para jogadores não-OP.
 */
public class OpPlayersManager {
    private static Set<UUID> opPlayers = new HashSet<>();

    public static void setOpPlayers(Set<UUID> players) {
        opPlayers = new HashSet<>(players);
    }

    public static boolean isOp(UUID playerUUID) {
        return opPlayers.contains(playerUUID);
    }

    public static Set<UUID> getOpPlayers() {
        return new HashSet<>(opPlayers);
    }

    public static void clear() {
        opPlayers.clear();
    }
}