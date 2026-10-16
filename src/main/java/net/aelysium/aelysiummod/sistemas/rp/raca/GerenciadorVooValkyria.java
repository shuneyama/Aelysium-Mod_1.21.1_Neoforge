package net.aelysium.aelysiummod.sistemas.rp.raca;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GerenciadorVooValkyria {

    public static final int MAX_FLY_TICKS  = 20 * 60 * 3;
    public static final int COOLDOWN_TICKS = 20 * 30;
    public static final float FLIGHT_SPEED = 0.02f;

    public static class DadosVoo {
        public int ticksFlown    = 0;
        public int cooldownTicks = 0;
        public boolean wasFlying = false;
    }

    private static final Map<UUID, DadosVoo> data = new HashMap<>();

    public static DadosVoo getOrCreate(UUID uuid) {
        return data.computeIfAbsent(uuid, k -> new DadosVoo());
    }

    public static DadosVoo get(UUID uuid) {
        return data.get(uuid);
    }

    public static void clearData(UUID uuid) {
        data.remove(uuid);
    }

    public static boolean isOnCooldown(UUID uuid) {
        DadosVoo d = data.get(uuid);
        return d != null && d.cooldownTicks > 0;
    }

    public static boolean hasFlightTime(UUID uuid) {
        DadosVoo d = data.get(uuid);
        return d == null || d.ticksFlown < MAX_FLY_TICKS;
    }
}