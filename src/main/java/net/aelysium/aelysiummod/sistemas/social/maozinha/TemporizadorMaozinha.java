package net.aelysium.aelysiummod.sistemas.social.maozinha;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class TemporizadorMaozinha {

    @SubscribeEvent
    public void aoTickServidor(ServerTickEvent.Post event) {
        GerenciadorMaozinha.tick(event.getServer());
    }
}
