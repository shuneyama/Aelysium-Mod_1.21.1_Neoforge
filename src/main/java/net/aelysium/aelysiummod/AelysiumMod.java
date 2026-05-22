package net.aelysium.aelysiummod;

import com.natamus.collective_common_neoforge.check.ShouldLoadCheck;
import net.aelysium.aelysiummod.holograma.GerenciadorHologramas;
import net.aelysium.aelysiummod.holograma.HologramaTicker;
import net.aelysium.aelysiummod.block.ModBlocks;
import net.aelysium.aelysiummod.chat.PlayerChatData;
import net.aelysium.aelysiummod.client.LuaClientRenderer;
import net.aelysium.aelysiummod.client.RenderFormaDivina;
import net.aelysium.aelysiummod.client.TeclasAelysium;
import net.aelysium.aelysiummod.client.ValkyriaHudRenderer;
import net.aelysium.aelysiummod.banlist.config.BanlistConfig;
import net.aelysium.aelysiummod.banlist.event.CuriosCompat;
import net.aelysium.aelysiummod.banlist.event.InventoryTickHandler;
import net.aelysium.aelysiummod.banlist.event.ItemBanEvents;
import net.aelysium.aelysiummod.banlist.network.BanlistNetwork;
import net.aelysium.aelysiummod.banlist.network.ClientBanData;
import net.aelysium.aelysiummod.comandos.AelysiumComandos;
import net.aelysium.aelysiummod.deus.FormaDivina;
import net.aelysium.aelysiummod.deus.VanishTicker;
import net.aelysium.aelysiummod.efeitos.ModEfeitos;
import net.aelysium.aelysiummod.holograma.network.HologramaClientEventHandler;
import net.aelysium.aelysiummod.item.ModItens;
import net.aelysium.aelysiummod.network.GlitchFogHandler;
import net.aelysium.aelysiummod.npc.client.renderer.CustomNpcRenderer;
import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
import net.aelysium.aelysiummod.particula.DamaDaNoiteParticula;
import net.aelysium.aelysiummod.particula.DamaVermelhaDaNoiteParticula;
import net.aelysium.aelysiummod.particula.ModParticles;
import net.aelysium.aelysiummod.protecao.evento.EventHandlerMovimento;
import net.aelysium.aelysiummod.protecao.evento.EventHandlerProtecao;
import net.aelysium.aelysiummod.protecao.evento.RenderizadorRegioes;
import net.aelysium.aelysiummod.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.raca.RaceLoginHandler;
import net.aelysium.aelysiummod.raca.RaceTicker;
import net.aelysium.aelysiummod.teleporte.TeleportTicker;
import net.aelysium.aelysiummod.util.AbaCriativo;
import net.aelysium.aelysiummod.whitelist.WhitelistOfflineManager;
import net.aelysium.aelysiummod.npc.NpcRegistry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

@Mod(AelysiumMod.MOD_ID)
public class AelysiumMod {
    public static final String MOD_ID = "aelysiummod";

    public AelysiumMod(IEventBus modEventBus, ModContainer modContainer) {
        if (!ShouldLoadCheck.shouldLoad(MOD_ID)) return;

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        AbaCriativo.register(modEventBus);
        ModItens.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModParticles.register(modEventBus);
        ModEfeitos.register(modEventBus);
        NpcRegistry.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        NeoForge.EVENT_BUS.register(new RaceTicker());
        NeoForge.EVENT_BUS.register(new VanishTicker());
        NeoForge.EVENT_BUS.register(new RaceLoginHandler());
        NeoForge.EVENT_BUS.register(new FormaDivina());
        NeoForge.EVENT_BUS.register(new EventHandlerProtecao());
        NeoForge.EVENT_BUS.register(new EventHandlerMovimento());
        NeoForge.EVENT_BUS.register(new TeleportTicker());
        NeoForge.EVENT_BUS.register(new HologramaTicker());

        BanlistConfig.load();
        NeoForge.EVENT_BUS.register(new ItemBanEvents());
        NeoForge.EVENT_BUS.register(new InventoryTickHandler());
        if (CuriosCompat.isCuriosLoaded()) {
            NeoForge.EVENT_BUS.register(new CuriosCompat());
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new AelysiumComandos());
        event.enqueueWork(() -> {
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.DAMA_DA_NOITE.getId(), ModBlocks.POTTED_DAMA_DA_NOITE);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.DAMA_VERMELHA_DA_NOITE.getId(), ModBlocks.POTTED_DAMA_VERMELHA_DA_NOITE);
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        PlayerChatData.setDataFolder(
                event.getServer().getWorldPath(LevelResource.ROOT).resolve("aelysium_balloon_data")
        );
        WhitelistOfflineManager.inicializar(event.getServer());
        GerenciadorRegioes.getInstance().setServidor(event.getServer());
        GerenciadorHologramas.getInstance().setServidor(event.getServer());
        BanlistConfig.load();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BanlistNetwork.syncToPlayer(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide()) {
            ClientBanData.clear();
        }
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class CommonModEvents {
        @SubscribeEvent
        public static void registerEntityAttributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) {
            event.put(NpcRegistry.CUSTOM_NPC.get(),
                    CustomNpcEntity.createAttributes().build());
        }
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            NeoForge.EVENT_BUS.register(new LuaClientRenderer());
            NeoForge.EVENT_BUS.register(new ValkyriaHudRenderer());
            NeoForge.EVENT_BUS.register(new RenderFormaDivina());
            NeoForge.EVENT_BUS.register(new TeclasAelysium());
            NeoForge.EVENT_BUS.register(new GlitchFogHandler());
            NeoForge.EVENT_BUS.register(RenderizadorRegioes.class);
            NeoForge.EVENT_BUS.register(new HologramaClientEventHandler());
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            TeclasAelysium.registrar(event);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(NpcRegistry.CUSTOM_NPC.get(),
                    CustomNpcRenderer::new);
        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.DAMA_DA_NOITE_PARTICULA.get(), DamaDaNoiteParticula.Provider::new);
            event.registerSpriteSet(ModParticles.DAMA_VERMELHA_DA_NOITE_PARTICULA.get(), DamaVermelhaDaNoiteParticula.Provider::new);
        }
    }
}