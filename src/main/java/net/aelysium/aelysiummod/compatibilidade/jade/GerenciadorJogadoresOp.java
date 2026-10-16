package net.aelysium.aelysiummod.compatibilidade.jade;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GerenciadorJogadoresOp {
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