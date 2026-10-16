package net.aelysium.aelysiummod.sistemas.mundo.dificuldade;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ControleVooDificuldade {

    private static final Map<UUID, Long> BLOQUEIOS = new HashMap<>();

    public static boolean estaVoando(ServerPlayer jogador) {
        return jogador.getAbilities().flying || jogador.isFallFlying();
    }

    public static void bloquear(ServerPlayer jogador, int ticks) {
        BLOQUEIOS.put(jogador.getUUID(), jogador.level().getGameTime() + ticks);
        cancelar(jogador);
    }

    public static void aoTick(ServerPlayer jogador) {
        Long fim = BLOQUEIOS.get(jogador.getUUID());
        if (fim == null) return;
        if (jogador.level().getGameTime() >= fim) {
            BLOQUEIOS.remove(jogador.getUUID());
            return;
        }
        cancelar(jogador);
    }

    public static void limpar(UUID jogador) {
        BLOQUEIOS.remove(jogador);
    }

    private static void cancelar(ServerPlayer jogador) {
        if (jogador.isFallFlying()) jogador.stopFallFlying();
        if (jogador.getAbilities().flying) {
            jogador.getAbilities().flying = false;
            jogador.onUpdateAbilities();
        }
    }
}
