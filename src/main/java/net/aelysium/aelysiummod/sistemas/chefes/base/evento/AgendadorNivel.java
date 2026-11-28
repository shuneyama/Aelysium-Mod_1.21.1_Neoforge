package net.aelysium.aelysiummod.sistemas.chefes.base.evento;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class AgendadorNivel {

    private static final Map<Level, EventScheduler> AGENDADORES = new WeakHashMap<>();

    private AgendadorNivel() {
    }

    public static synchronized EventScheduler de(Level nivel) {
        return AGENDADORES.computeIfAbsent(nivel, n -> new EventScheduler());
    }

    @SubscribeEvent
    public static void aoTickNivel(LevelTickEvent.Post evento) {
        EventScheduler agendador;
        synchronized (AgendadorNivel.class) {
            agendador = AGENDADORES.get(evento.getLevel());
        }
        if (agendador != null) agendador.updateEvents();
    }
}
