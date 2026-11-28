package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ComumFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.RegistroFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.gui.CustomBossBar;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.particula.ParticleCloud;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.particula.ParticleRing;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.particula.ParticleSnowFlake;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render.entidade.FrozenRenderHandler;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render.entidade.camada.CamadaCristal;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render.entidade.RenderFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render.entidade.RenderIceBall;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render.entidade.RenderNothing;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.dados.FrozenData;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.EntidadesAtivas;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.efeitos.EntityCameraShake;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.EntityFrozenController;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public final class ClienteFrostmaw {
    private static final EntidadesAtivas<EntityCameraShake> TREMORES = new EntidadesAtivas<>();

    private static final ResourceLocation BORRAO_CONGELADO = ResourceLocation.withDefaultNamespace("textures/misc/powder_snow_outline.png");

    private ClienteFrostmaw() {
    }

    @SubscribeEvent
    public static void aoConfigurarCliente(FMLClientSetupEvent evento) {
        ComumFrostmaw.PROXY = new ProxyClienteFrostmaw();
    }

    @SubscribeEvent
    public static void registrarRenderizadores(EntityRenderersEvent.RegisterRenderers evento) {
        evento.registerEntityRenderer(RegistroFrostmaw.FROSTMAW.get(), RenderFrostmaw::new);
        evento.registerEntityRenderer(RegistroFrostmaw.ICE_BALL.get(), RenderIceBall::new);
        evento.registerEntityRenderer(RegistroFrostmaw.ICE_BREATH.get(), RenderNothing::new);
        evento.registerEntityRenderer(RegistroFrostmaw.FROZEN_CONTROLLER.get(), RenderNothing::new);
        evento.registerEntityRenderer(RegistroFrostmaw.CAMERA_SHAKE.get(), RenderNothing::new);
        evento.registerEntityRenderer(RegistroFrostmaw.BLOCK_SWAPPER.get(), RenderNothing::new);
    }

    @SubscribeEvent
    public static void registrarModelos(ModelEvent.RegisterAdditional evento) {
        evento.register(CamadaCristal.MODELO);
    }

    @SubscribeEvent
    public static void registrarParticulas(RegisterParticleProvidersEvent evento) {
        evento.registerSpriteSet(RegistroFrostmaw.SNOWFLAKE.get(), ParticleSnowFlake.Provider::new);
        evento.registerSpriteSet(RegistroFrostmaw.CLOUD.get(), ParticleCloud.Provider::new);
        evento.registerSpriteSet(RegistroFrostmaw.RING.get(), ParticleRing.Provider::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @SubscribeEvent
    public static void adicionarCamadas(EntityRenderersEvent.AddLayers evento) {
        for (EntityType<?> tipo : BuiltInRegistries.ENTITY_TYPE) {
            if (tipo == EntityType.ENDER_DRAGON || tipo == EntityType.PLAYER) continue;
            try {
                if (evento.getRenderer((EntityType) tipo) instanceof LivingEntityRenderer<?, ?> renderizador) {
                    renderizador.addLayer(new FrozenRenderHandler.LayerFrozen(renderizador));
                }
            } catch (Exception ignorada) {
            }
        }
        for (PlayerSkin.Model pele : evento.getSkins()) {
            if (evento.getSkin(pele) instanceof LivingEntityRenderer<?, ?> renderizador) {
                renderizador.addLayer(new FrozenRenderHandler.LayerFrozen(renderizador));
            }
        }
    }

    @SubscribeEvent
    public static void aoRenderizarMao(RenderHandEvent evento) {
        FrozenRenderHandler.onRenderHand(evento);
    }

    @SubscribeEvent
    public static void aoRenderizarQuadro(RenderFrameEvent.Pre evento) {
        Player jogador = Minecraft.getInstance().player;
        if (jogador == null || !jogador.hasData(RegistroFrostmaw.FROZEN_DATA)) return;
        FrozenData dados = jogador.getData(RegistroFrostmaw.FROZEN_DATA);
        if (dados.getFrozen() && dados.getPrevFrozen()) {
            jogador.setYRot(dados.getFrozenYaw());
            jogador.setXRot(dados.getFrozenPitch());
            jogador.yHeadRot = dados.getFrozenYawHead();
            jogador.yRotO = jogador.getYRot();
            jogador.xRotO = jogador.getXRot();
            jogador.yHeadRotO = jogador.yHeadRot;
        }
    }

    @SubscribeEvent
    public static void aoRenderizarVivo(RenderLivingEvent.Pre<?, ?> evento) {
        LivingEntity entidade = evento.getEntity();
        if (!entidade.hasData(RegistroFrostmaw.FROZEN_DATA)) return;
        FrozenData dados = entidade.getData(RegistroFrostmaw.FROZEN_DATA);
        if (dados.getFrozen() && dados.getPrevFrozen()) {
            entidade.setYRot(entidade.yRotO = dados.getFrozenYaw());
            entidade.setXRot(entidade.xRotO = dados.getFrozenPitch());
            entidade.yHeadRot = entidade.yHeadRotO = dados.getFrozenYawHead();
            entidade.yBodyRot = entidade.yBodyRotO = dados.getFrozenRenderYawOffset();
            entidade.attackAnim = entidade.oAttackAnim = dados.getFrozenSwingProgress();
            entidade.walkAnimation.setSpeed(0);
            entidade.walkAnimation.update(0, 0);
            entidade.setShiftKeyDown(false);
        }
    }

    @SubscribeEvent
    public static void aoRenderizarSobreposicao(RenderGuiLayerEvent.Post evento) {
        if (evento.getName() != VanillaGuiLayers.CAMERA_OVERLAYS) return;
        LocalPlayer jogador = Minecraft.getInstance().player;
        if (jogador == null || !jogador.hasData(RegistroFrostmaw.FROZEN_DATA)) return;
        if (jogador.getData(RegistroFrostmaw.FROZEN_DATA).getFrozen() && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) {
            GuiGraphics graficos = evento.getGuiGraphics();
            graficos.blit(BORRAO_CONGELADO, 0, 0, 0, 0, graficos.guiWidth(), graficos.guiHeight(), graficos.guiWidth(), graficos.guiHeight());
        }
    }

    @SubscribeEvent
    public static void aoRenderizarHud(RenderGuiLayerEvent.Pre evento) {
        LocalPlayer jogador = Minecraft.getInstance().player;
        if (jogador != null && jogador.getVehicle() instanceof EntityFrozenController) {
            if (evento.getName() == VanillaGuiLayers.VEHICLE_HEALTH) {
                evento.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registrarTremor(EntityJoinLevelEvent evento) {
        if (evento.getLevel().isClientSide() && evento.getEntity() instanceof EntityCameraShake tremor) TREMORES.adicionar(tremor);
    }

    @SubscribeEvent
    public static void removerTremor(EntityLeaveLevelEvent evento) {
        if (evento.getLevel().isClientSide() && evento.getEntity() instanceof EntityCameraShake tremor) TREMORES.remover(tremor);
    }

    @SubscribeEvent
    public static void aoAjustarCamera(ViewportEvent.ComputeCameraAngles evento) {
        Player jogador = Minecraft.getInstance().player;
        if (jogador == null || !RegistroFrostmaw.TREMER_CAMERA || Minecraft.getInstance().isPaused()) return;
        float parcial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        float tempo = jogador.tickCount + parcial;
        float amplitude = 0;
        for (EntityCameraShake tremor : TREMORES.todas()) {
            if (tremor.level() == jogador.level() && tremor.distanceTo(jogador) < tremor.getRadius()) {
                amplitude += tremor.getShakeAmount(jogador, parcial);
            }
        }
        if (amplitude <= 0) return;
        if (amplitude > 1.0F) amplitude = 1.0F;
        evento.setPitch((float) (evento.getPitch() + amplitude * Math.cos(tempo * 3 + 2) * 25));
        evento.setYaw((float) (evento.getYaw() + amplitude * Math.cos(tempo * 5 + 1) * 25));
        evento.setRoll((float) (evento.getRoll() + amplitude * Math.cos(tempo * 4) * 25));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void aoRenderizarBarraChefe(CustomizeGuiOverlayEvent.BossEventProgress evento) {
        ResourceLocation nome = CustomBossBar.bossBarRegistryNames.get(evento.getBossEvent().getId());
        if (nome == null) return;
        CustomBossBar barra = CustomBossBar.customBossBars.get(nome);
        if (barra == null) return;
        evento.setCanceled(true);
        barra.renderBossBar(evento);
    }
}
