package net.aelysium.aelysiummod.deus;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VanishTransitionHandler {
    public enum Phase {
        GATHERING,
        WAITING,
        VISIBLE,
        DISPERSING
    }

    public static class TransitionData {
        public final ServerPlayer player;
        public final DeusType deus;
        public final boolean goingToSpectator;
        public final GameType previousGamemode;
        public Phase phase = Phase.GATHERING;
        public int tickCount = 0;

        public TransitionData(ServerPlayer player, DeusType deus, boolean goingToSpectator, GameType previousGamemode) {
            this.player = player;
            this.deus = deus;
            this.goingToSpectator = goingToSpectator;
            this.previousGamemode = previousGamemode;
        }
    }

    private static final Map<UUID, GameType> lastNonSpectatorMode = new ConcurrentHashMap<>();
    public static final Map<UUID, TransitionData> activeTransitions = new ConcurrentHashMap<>();

    public static void startTransition(ServerPlayer player, DeusType deus) {
        UUID id = player.getUUID();

        if (activeTransitions.containsKey(id)) return;

        GameType current = player.gameMode.getGameModeForPlayer();
        boolean goingToSpectator = current != GameType.SPECTATOR;
        GameType previous = goingToSpectator
                ? current
                : lastNonSpectatorMode.getOrDefault(id, GameType.SURVIVAL);

        if (goingToSpectator) {
            lastNonSpectatorMode.put(id, current);
        }

        activeTransitions.put(id, new TransitionData(player, deus, goingToSpectator, previous));
    }

    public static void removeTransition(UUID id) {
        activeTransitions.remove(id);
    }


}
