package net.aelysium.aelysiummod.sistemas.rp.raca;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;

public class AlternadorVisaoNoturna {

    private static final Set<UUID> ativos = ConcurrentHashMap.newKeySet();

    public static boolean racaElegivel(TipoRaca race) {
        return race == TipoRaca.ELVARIN || race == TipoRaca.DRACONO
                || race == TipoRaca.TIEFLING || race == TipoRaca.VALKYRIA;
    }

    public static boolean estaAtiva(UUID uuid) {
        return ativos.contains(uuid);
    }

    public static void alternar(ServerPlayer player) {
        TipoRaca race = TemporizadorRaca.getCachedRace(player.getUUID());
        if (!racaElegivel(race)) return;

        UUID uuid = player.getUUID();
        if (ativos.remove(uuid)) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        } else {
            ativos.add(uuid);
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
        }
    }

    public static void remover(UUID uuid) {
        ativos.remove(uuid);
    }
}
