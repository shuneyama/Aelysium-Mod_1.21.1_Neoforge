package net.aelysium.aelysiummod.conteudo.filhotes.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador.*;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegistroClienteFilhotes {

    @SubscribeEvent
    public static void registrarCamadas(EntityRenderersEvent.RegisterLayerDefinitions event) {
        CamadasFilhotes.registrar(event);
        CamadasMonstrosFilhotes.registrar(event);
    }

    @SubscribeEvent
    public static void registrarRenderizadores(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.COW, RenderizadorVaca::new);
        event.registerEntityRenderer(EntityType.MOOSHROOM, RenderizadorMooshroom::new);
        event.registerEntityRenderer(EntityType.PIG, RenderizadorPorco::new);
        event.registerEntityRenderer(EntityType.CHICKEN, RenderizadorGalinha::new);
        event.registerEntityRenderer(EntityType.SHEEP, RenderizadorOvelha::new);
        event.registerEntityRenderer(EntityType.POLAR_BEAR, RenderizadorUrsoPolar::new);
        event.registerEntityRenderer(EntityType.TURTLE, RenderizadorTartaruga::new);
        event.registerEntityRenderer(EntityType.GOAT, RenderizadorCabra::new);
        event.registerEntityRenderer(EntityType.PANDA, RenderizadorPanda::new);
        event.registerEntityRenderer(EntityType.WOLF, RenderizadorLobo::new);
        event.registerEntityRenderer(EntityType.CAT, RenderizadorGato::new);
        event.registerEntityRenderer(EntityType.OCELOT, RenderizadorJaguatirica::new);
        event.registerEntityRenderer(EntityType.FOX, RenderizadorRaposa::new);
        event.registerEntityRenderer(EntityType.SQUID, RenderizadorLula::new);
        event.registerEntityRenderer(EntityType.GLOW_SQUID, RenderizadorLulaBrilhante::new);
        event.registerEntityRenderer(EntityType.DOLPHIN, RenderizadorGolfinho::new);
        RenderizadoresSimples.registrar(event);
        RenderizadoresMonstros.registrar(event);
    }

    @SubscribeEvent
    public static void registrarRecarga(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) gerenciador -> UtilFilhotes.limparCache());
    }
}
