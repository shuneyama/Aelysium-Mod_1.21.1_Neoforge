package net.aelysium.aelysiummod.nucleo.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class VersaoFonteCliente {

    private static volatile int versao;

    private VersaoFonteCliente() {}

    public static int atual() {
        return versao;
    }

    @SubscribeEvent
    public static void registrar(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) gerenciador -> versao++);
    }
}
