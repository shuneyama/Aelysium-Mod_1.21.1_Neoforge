package net.aelysium.aelysiummod.holograma.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

public class HologramaClientEventHandler {

    @SubscribeEvent
    public void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        HologramaClientHandler.limparTudo();
    }
}