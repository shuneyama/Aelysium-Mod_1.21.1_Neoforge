package net.aelysium.aelysiummod.sistemas.rp.deus;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class UtilDeus {
    private static final Map<UUID, TipoDeus> DEUSES = new ConcurrentHashMap<>();

    private UtilDeus() {
    }

    public static boolean ehDeus(Entity entidade) {
        return entidade instanceof ServerPlayer jogador && DEUSES.containsKey(jogador.getUUID());
    }

    public static boolean poder(Entity entidade, int poder) {
        if (!(entidade instanceof ServerPlayer jogador)) return false;
        TipoDeus deus = DEUSES.get(jogador.getUUID());
        return deus != null && deus.tem(poder);
    }

    public static void atualizar(UUID uuid, TipoDeus deus) {
        if (deus != null && deus != TipoDeus.NONE) {
            DEUSES.put(uuid, deus);
        } else {
            DEUSES.remove(uuid);
        }
    }

    @SubscribeEvent
    public static void aoIniciarServidor(ServerStartedEvent evento) {
        DEUSES.clear();
        for (Map.Entry<UUID, TipoDeus> entrada : DadosDeus.get(evento.getServer().overworld()).todos().entrySet()) {
            if (entrada.getValue() != TipoDeus.NONE) DEUSES.put(entrada.getKey(), entrada.getValue());
        }
    }

    @SubscribeEvent
    public static void aoPararServidor(ServerStoppedEvent evento) {
        DEUSES.clear();
    }
}
