package net.aelysium.aelysiummod.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FormaDivinaCache {

    private static final Map<UUID, DivinaState> states = new ConcurrentHashMap<>();

    public record DivinaState(String deusId, int cor) {}

    public static void set(UUID uuid, boolean ativa, String deusId, int cor) {
        if (ativa) {
            states.put(uuid, new DivinaState(deusId, cor));
        } else {
            states.remove(uuid);
        }
    }

    public static boolean isActive(UUID uuid) {
        return states.containsKey(uuid);
    }

    public static DivinaState get(UUID uuid) {
        return states.get(uuid);
    }

    public static void clear() {
        states.clear();
    }
}