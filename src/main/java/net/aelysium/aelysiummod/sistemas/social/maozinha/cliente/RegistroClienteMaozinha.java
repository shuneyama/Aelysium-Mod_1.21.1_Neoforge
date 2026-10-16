package net.aelysium.aelysiummod.sistemas.social.maozinha.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegistroClienteMaozinha {

    @SubscribeEvent
    public static void aoConfigurarCliente(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new OrientacaoLiderMaozinha());
        NeoForge.EVENT_BUS.register(new ManipuladorPuxaoCliente());
        NeoForge.EVENT_BUS.register(new ManipuladorArrastoLiderCliente());
        NeoForge.EVENT_BUS.register(new SuavizadorPosicaoMaozinha());
        NeoForge.EVENT_BUS.addListener(RegistroClienteMaozinha::aoSairDoMundo);
    }

    private static void aoSairDoMundo(ClientPlayerNetworkEvent.LoggingOut event) {
        CacheMaozinha.limpar();
        SuavizadorPosicaoMaozinha.limpar();
        AnimacaoBracoMaozinha.limpar();
    }
}
