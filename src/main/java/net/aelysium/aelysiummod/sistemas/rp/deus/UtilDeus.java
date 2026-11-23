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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class UtilDeus {
    private static final Set<UUID> DEUSES = ConcurrentHashMap.newKeySet();

    private UtilDeus() {
    }

    public static boolean ehDeus(Entity entidade) {
        return entidade instanceof ServerPlayer jogador && DEUSES.contains(jogador.getUUID());
    }

    public static void atualizar(UUID uuid, boolean deus) {
        if (deus) {
            DEUSES.add(uuid);
        } else {
            DEUSES.remove(uuid);
        }
    }

    @SubscribeEvent
    public static void aoIniciarServidor(ServerStartedEvent evento) {
        DEUSES.clear();
        for (Map.Entry<UUID, TipoDeus> entrada : DadosDeus.get(evento.getServer().overworld()).todos().entrySet()) {
            if (entrada.getValue() != TipoDeus.NONE) DEUSES.add(entrada.getKey());
        }
    }

    @SubscribeEvent
    public static void aoPararServidor(ServerStoppedEvent evento) {
        DEUSES.clear();
    }
}
