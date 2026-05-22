package net.aelysium.aelysiummod.raca;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ValkyriaFlightManager {

    public static final int MAX_FLY_TICKS  = 20 * 60 * 3;
    public static final int COOLDOWN_TICKS = 20 * 30;
    public static final float FLIGHT_SPEED = 0.025f;

    public static class FlightData {
        public int ticksFlown    = 0;
        public int cooldownTicks = 0;
        public boolean wasFlying = false;
    }

    private static final Map<UUID, FlightData> data = new HashMap<>();

    public static FlightData getOrCreate(UUID uuid) {
        return data.computeIfAbsent(uuid, k -> new FlightData());
    }

    public static FlightData get(UUID uuid) {
        return data.get(uuid);
    }

    public static void clearData(UUID uuid) {
        data.remove(uuid);
    }

    public static boolean isOnCooldown(UUID uuid) {
        FlightData d = data.get(uuid);
        return d != null && d.cooldownTicks > 0;
    }

    public static boolean hasFlightTime(UUID uuid) {
        FlightData d = data.get(uuid);
        return d == null || d.ticksFlown < MAX_FLY_TICKS;
    }
}