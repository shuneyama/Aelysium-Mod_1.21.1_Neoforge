package net.aelysium.aelysiummod.sistemas.midia.tocar.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.cliente.VolumeSomCliente;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class RegistroClienteTocar {

    @SubscribeEvent
    public static void aoConfigurarCliente(FMLClientSetupEvent event) {
        ConfiguracaoSomClienteAelysium.carregar();
        VolumeSomCliente.registrar(ConfiguracaoSomClienteAelysium::getVolumeNormalizado);
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut evento) -> ManipuladorTocarCliente.pararTudo());
    }
}
