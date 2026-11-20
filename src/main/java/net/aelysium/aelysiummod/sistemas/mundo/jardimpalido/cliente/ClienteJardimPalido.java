package net.aelysium.aelysiummod.sistemas.mundo.jardimpalido.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.mundo.jardimpalido.ModJardimPalido;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class ClienteJardimPalido {

    @SubscribeEvent
    public static void camadas(EntityRenderersEvent.RegisterLayerDefinitions evento) {
        evento.registerLayerDefinition(RenderizadorCreaking.CAMADA, ModeloCreaking::criarCamada);
    }

    @SubscribeEvent
    public static void renderizadores(EntityRenderersEvent.RegisterRenderers evento) {
        evento.registerEntityRenderer(ModJardimPalido.CREAKING.get(), RenderizadorCreaking::new);
    }
}
