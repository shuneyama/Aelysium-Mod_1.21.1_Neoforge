package net.aelysium.aelysiummod.system;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.UUID;

public class EffectTicker {

    private static final HashMap<UUID, Integer> timers = new HashMap<>();

    public static boolean shouldTrigger(ServerPlayer p, int seconds) {
        int ticks = seconds * 20;

        int current = timers.getOrDefault(p.getUUID(), 0);

        if (current >= ticks) {
            timers.put(p.getUUID(), 0);
            return true;
        }

        timers.put(p.getUUID(), current + 1);
        return false;
    }
}
