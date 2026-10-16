package net.aelysium.aelysiummod.sistemas.rp.deus;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;

public class ManipuladorTransicaoVanish {
    public enum Fase {
        GATHERING,
        WAITING,
        VISIBLE,
        DISPERSING
    }

    public static class DadosTransicao {
        public final ServerPlayer player;
        public final TipoDeus deus;
        public final boolean goingToSpectator;
        public final GameType previousGamemode;
        public Fase phase = Fase.GATHERING;
        public int tickCount = 0;

        public DadosTransicao(ServerPlayer player, TipoDeus deus, boolean goingToSpectator, GameType previousGamemode) {
            this.player = player;
            this.deus = deus;
            this.goingToSpectator = goingToSpectator;
            this.previousGamemode = previousGamemode;
        }
    }

    private static final Map<UUID, GameType> lastNonSpectatorMode = new ConcurrentHashMap<>();
    public static final Map<UUID, DadosTransicao> activeTransitions = new ConcurrentHashMap<>();

    public static void startTransition(ServerPlayer player, TipoDeus deus) {
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

        activeTransitions.put(id, new DadosTransicao(player, deus, goingToSpectator, previous));
    }

    public static void removeTransition(UUID id) {
        activeTransitions.remove(id);
    }


}
