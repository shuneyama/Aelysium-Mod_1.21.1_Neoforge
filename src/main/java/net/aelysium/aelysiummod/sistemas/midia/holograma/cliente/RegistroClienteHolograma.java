package net.aelysium.aelysiummod.sistemas.midia.holograma.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class RegistroClienteHolograma {

    @SubscribeEvent
    public static void aoConfigurarCliente(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ManipuladorEventosHologramaCliente());
    }
}
