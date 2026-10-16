package net.aelysium.aelysiummod.sistemas.rp.deus.cliente;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CacheFormaDivina {

    private static final Map<UUID, EstadoDivina> states = new ConcurrentHashMap<>();

    public record EstadoDivina(String deusId, int cor) {}

    public static void set(UUID uuid, boolean ativa, String deusId, int cor) {
        if (ativa) {
            states.put(uuid, new EstadoDivina(deusId, cor));
        } else {
            states.remove(uuid);
        }
    }

    public static boolean isActive(UUID uuid) {
        return states.containsKey(uuid);
    }

    public static EstadoDivina get(UUID uuid) {
        return states.get(uuid);
    }

    public static void clear() {
        states.clear();
    }
}