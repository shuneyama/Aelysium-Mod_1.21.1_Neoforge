package net.aelysium.aelysiummod.teleporte;

import net.aelysium.aelysiummod.deus.DeusType;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportTransitionHandler {

    public enum Phase {
        GATHERING,
        WAITING,
        DISPERSING,
        DONE
    }

    public static class TransitionData {
        public final ServerPlayer player;
        public final DeusType deus;
        public final double destX, destY, destZ;
        public final float destYaw, destPitch;
        public final boolean temCoordenada;
        public Phase phase = Phase.GATHERING;
        public int tickCount = 0;
        public boolean glowingAtivo = true;
        public int glowToggleTicks = 0;

        public TransitionData(ServerPlayer player, DeusType deus,
                              double destX, double destY, double destZ,
                              float destYaw, float destPitch, boolean temCoordenada) {
            this.player = player;
            this.deus = deus;
            this.destX = destX;
            this.destY = destY;
            this.destZ = destZ;
            this.destYaw = destYaw;
            this.destPitch = destPitch;
            this.temCoordenada = temCoordenada;
        }
    }

    public static final Map<UUID, TransitionData> activeTransitions = new ConcurrentHashMap<>();

    public static void startTransition(ServerPlayer player, DeusType deus,
                                       double x, double y, double z,
                                       float yaw, float pitch, boolean temCoordenada) {
        UUID id = player.getUUID();
        if (activeTransitions.containsKey(id)) return;
        activeTransitions.put(id, new TransitionData(player, deus, x, y, z, yaw, pitch, temCoordenada));
    }

    public static void removeTransition(UUID id) {
        activeTransitions.remove(id);
    }

    public static boolean isInTransition(UUID id) {
        return activeTransitions.containsKey(id);
    }
}