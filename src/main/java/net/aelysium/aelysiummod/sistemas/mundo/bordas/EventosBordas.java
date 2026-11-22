package net.aelysium.aelysiummod.sistemas.mundo.bordas;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class EventosBordas {

    private EventosBordas() {
    }

    @SubscribeEvent
    public static void aoIniciar(ServerStartingEvent evento) {
        BordasPorDimensao.isolar(evento.getServer());
    }

    @SubscribeEvent
    public static void aoParar(ServerStoppingEvent evento) {
        BordasPorDimensao.salvarTodas(evento.getServer());
    }

    @SubscribeEvent
    public static void aoSalvar(LevelEvent.Save evento) {
        if (evento.getLevel() instanceof ServerLevel nivel) {
            BordasPorDimensao.salvar(nivel);
        }
    }
}
