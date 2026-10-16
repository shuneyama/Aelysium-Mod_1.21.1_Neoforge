package net.aelysium.aelysiummod.conteudo.item.glock.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModEntidades;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegistroClienteGlock {

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void aoConfigurarCliente(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new RenderizadorHudGlock());
        NeoForge.EVENT_BUS.register(new ManipuladorRecuoGlock());
    }

    @SubscribeEvent
    public static void registrarRenderizadores(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntidades.GLOCK_BALA.get(), RenderizadorBalaGlock::new);
    }
}
