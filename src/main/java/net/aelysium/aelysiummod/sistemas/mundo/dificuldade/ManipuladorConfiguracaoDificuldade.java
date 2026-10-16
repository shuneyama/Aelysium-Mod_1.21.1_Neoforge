package net.aelysium.aelysiummod.sistemas.mundo.dificuldade;

import net.aelysium.aelysiummod.AelysiumMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorConfiguracaoDificuldade {

    @SubscribeEvent
    public static void aoIniciarServidor(ServerStartingEvent evento) {
        ConfiguracaoDificuldade.carregar(evento.getServer());
    }
}
