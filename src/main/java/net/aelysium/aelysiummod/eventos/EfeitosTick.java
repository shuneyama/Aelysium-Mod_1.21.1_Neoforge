package net.aelysium.aelysiummod.eventos;

import java.util.HashMap;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class EfeitosTick {
    private static final HashMap<UUID, Integer> timers = new HashMap<>();

    /**
     * Verifica se o efeito deve ser disparado para o jogador.
     * Retorna true a cada X segundos para o jogador específico.
     *
     * @param player  O jogador
     * @param seconds Intervalo em segundos
     * @return true se o efeito deve ser aplicado agora
     */
    public static boolean shouldTrigger(ServerPlayer player, int seconds) {
        int ticks = seconds * 20;
        int current = timers.getOrDefault(player.getUUID(), 0);

        if (current >= ticks) {
            timers.put(player.getUUID(), 0);
            return true;
        }

        timers.put(player.getUUID(), current + 1);
        return false;
    }

    /**
     * Remove o timer de um jogador (útil quando ele sai do servidor)
     */
    public static void removePlayer(UUID uuid) {
        timers.remove(uuid);
    }

    /**
     * Limpa todos os timers
     */
    public static void clearAll() {
        timers.clear();
    }
}