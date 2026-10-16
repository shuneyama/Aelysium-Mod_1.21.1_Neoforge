package net.aelysium.aelysiummod.sistemas.midia.holograma.cliente;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

public class ManipuladorEventosHologramaCliente {

    @SubscribeEvent
    public void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        ManipuladorHologramaCliente.limparTudo();
    }
}