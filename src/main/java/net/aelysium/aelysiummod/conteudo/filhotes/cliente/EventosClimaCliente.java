package net.aelysium.aelysiummod.conteudo.filhotes.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.filhotes.clima.CacheClimaCliente;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public final class EventosClimaCliente {

    private EventosClimaCliente() {
    }

    @SubscribeEvent
    public static void aoSair(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            CacheClimaCliente.remover(event.getEntity().getId());
        }
    }

    @SubscribeEvent
    public static void aoDesconectar(ClientPlayerNetworkEvent.LoggingOut event) {
        CacheClimaCliente.limpar();
    }

    @SubscribeEvent
    public static void aoConectar(ClientPlayerNetworkEvent.LoggingIn event) {
        CacheClimaCliente.limpar();
    }
}
