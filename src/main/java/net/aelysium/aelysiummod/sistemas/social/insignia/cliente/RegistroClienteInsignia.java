package net.aelysium.aelysiummod.sistemas.social.insignia.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.cliente.TelasCliente;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegistroClienteInsignia {

    @SubscribeEvent
    public static void aoConfigurarCliente(FMLClientSetupEvent event) {
        TelasCliente.registrarSeletorInsignia(TelaSeletorInsignia::new);
        TelasCliente.registrarCriadorInsignia(TelaCriarInsignia::new);
    }
}
