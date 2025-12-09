package net.aelysium.aelysiummod;

import com.natamus.collective_common_neoforge.check.ShouldLoadCheck;
import net.aelysium.aelysiummod.block.ModBlocks;
import net.aelysium.aelysiummod.comandos.AelysiumComandos;
import net.aelysium.aelysiummod.config.CarregarConfigs;
import net.aelysium.aelysiummod.config.racas.*;
import net.aelysium.aelysiummod.efeitos.ModEfeitos;
import net.aelysium.aelysiummod.eventos.racas.*;
import net.aelysium.aelysiummod.item.ModItens;
import net.aelysium.aelysiummod.jade.HiddenTeamSyncPacket;
import net.aelysium.aelysiummod.jade.OpPlayersSyncPacket;
import net.aelysium.aelysiummod.particula.DamaDaNoiteParticula;
import net.aelysium.aelysiummod.particula.DamaVermelhaDaNoiteParticula;
import net.aelysium.aelysiummod.particula.ModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(AelysiumMod.MOD_ID)
public class AelysiumMod {
    public static final String MOD_ID = "aelysiummod";

    public AelysiumMod(IEventBus modEventBus, ModContainer modContainer) {
        // Verificação do Collective (dependência)
        if (!ShouldLoadCheck.shouldLoad(MOD_ID)) {
            return;
        }

        // Registra listeners no MOD bus
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPackets);
        modEventBus.addListener(this::addCreative);

        // Registra conteúdo do mod
        AbaCriativo.register(modEventBus);
        ModItens.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModParticles.register(modEventBus);
        ModEfeitos.register(modEventBus);

        // Registra no GAME bus (NeoForge)
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(CarregarConfigs.class);

        // Registra handlers de efeitos de raças
        NeoForge.EVENT_BUS.register(new DeusEfeito());
        NeoForge.EVENT_BUS.register(new DraconoEfeito());
        NeoForge.EVENT_BUS.register(new ElvarinEfeito());
        NeoForge.EVENT_BUS.register(new HumanoEfeito());
        NeoForge.EVENT_BUS.register(new TieflingEfeito());
        NeoForge.EVENT_BUS.register(new UndyneEfeito());
        NeoForge.EVENT_BUS.register(new ValkyriaEfeito());
    }

    private void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Pacotes client-bound (servidor -> cliente)
        registrar.playToClient(
                HiddenTeamSyncPacket.TYPE,
                HiddenTeamSyncPacket.STREAM_CODEC,
                HiddenTeamSyncPacket::handle
        );
        registrar.playToClient(
                OpPlayersSyncPacket.TYPE,
                OpPlayersSyncPacket.STREAM_CODEC,
                OpPlayersSyncPacket::handle
        );
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new AelysiumComandos());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Adiciona itens às abas criativas aqui se necessário
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Lógica de inicialização do servidor aqui se necessário
    }

    // ==================== Client Events (Inner Class) ====================

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Carrega configurações do cliente para cada raça
            Deus_Config.loadClient();
            Dracono_Config.loadClient();
            Elvarin_Config.loadClient();
            Humano_Config.loadClient();
            Tiefling_Config.loadClient();
            Undyne_Config.loadClient();
            Valkyria_Config.loadClient();
        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(
                    ModParticles.DAMA_DA_NOITE_PARTICULA.get(),
                    DamaDaNoiteParticula.Provider::new
            );
            event.registerSpriteSet(
                    ModParticles.DAMA_VERMELHA_DA_NOITE_PARTICULA.get(),
                    DamaVermelhaDaNoiteParticula.Provider::new
            );
        }
    }
}