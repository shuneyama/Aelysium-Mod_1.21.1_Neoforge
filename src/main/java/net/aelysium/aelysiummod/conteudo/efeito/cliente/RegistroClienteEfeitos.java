package net.aelysium.aelysiummod.conteudo.efeito.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegistroClienteEfeitos {

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void registrarNevoaGlitch(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ManipuladorNevoaGlitch());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registrarEfeitoChoque(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new RenderizadorEfeitoChoque());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void registrarCongelar(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ManipuladorCongelarCliente());
    }
}
