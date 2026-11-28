package net.aelysium.aelysiummod.sistemas.chefes.base.combate;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class EventosChefes {
    private EventosChefes() {
    }

    @SubscribeEvent
    public static void aoIniciarServidor(ServerAboutToStartEvent evento) {
        ConfiguracaoChefes.carregar();
    }

    @SubscribeEvent
    public static void aoRecarregar(AddReloadListenerEvent evento) {
        evento.addListener((ResourceManagerReloadListener) gerenciador -> {
            ConfiguracaoChefes.carregar();
            MinecraftServer servidor = ServerLifecycleHooks.getCurrentServer();
            if (servidor != null) servidor.execute(() -> reaplicar(servidor));
        });
    }

    private static void reaplicar(MinecraftServer servidor) {
        for (ServerLevel nivel : servidor.getAllLevels()) {
            for (Entity entidade : nivel.getAllEntities()) {
                if (entidade instanceof ChefeAelysium chefe && entidade instanceof LivingEntity vivo) aplicarStatus(vivo, chefe);
            }
        }
    }

    @SubscribeEvent
    public static void aoEntrarNoMundo(EntityJoinLevelEvent evento) {
        if (evento.getLevel().isClientSide()) return;
        if (evento.getEntity() instanceof ChefeAelysium chefe && evento.getEntity() instanceof LivingEntity vivo) aplicarStatus(vivo, chefe);
    }

    public static void aplicarStatus(LivingEntity vivo, ChefeAelysium chefe) {
        StatusChefe status = ConfiguracaoChefes.status(chefe.chaveConfig());
        AttributeInstance vida = vivo.getAttribute(Attributes.MAX_HEALTH);
        if (vida != null && vida.getBaseValue() != status.vida()) {
            boolean cheia = vivo.getHealth() >= vivo.getMaxHealth();
            vida.setBaseValue(status.vida());
            if (cheia || vivo.getHealth() > vivo.getMaxHealth()) vivo.setHealth(vivo.getMaxHealth());
        }
        AttributeInstance armadura = vivo.getAttribute(Attributes.ARMOR);
        if (armadura != null && armadura.getBaseValue() != status.armadura()) armadura.setBaseValue(status.armadura());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void limitarDano(LivingDamageEvent.Pre evento) {
        if (!(evento.getEntity() instanceof ChefeAelysium chefe)) return;
        if (evento.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        float limite = ConfiguracaoChefes.status(chefe.chaveConfig()).capDano();
        if (limite > 0 && evento.getNewDamage() > limite) evento.setNewDamage(limite);
    }

    @SubscribeEvent
    public static void registrarAmeaca(LivingDamageEvent.Post evento) {
        if (evento.getEntity() instanceof ChefeAelysium chefe && !evento.getEntity().level().isClientSide()) {
            chefe.getAlvos().registrarDano(evento.getSource().getEntity(), evento.getNewDamage());
        }
    }

    @SubscribeEvent
    public static void aoTickServidor(ServerTickEvent.Post evento) {
        QuebraArmadura.tick();
        ChaoArmadilha.tick(evento.getServer());
    }

    @SubscribeEvent
    public static void aoDesligarServidor(ServerStoppingEvent evento) {
        ChaoArmadilha.restaurarTudo();
    }

    public static boolean podeCurar(ChefeAelysium chefe, StatusChefe status) {
        return status.curaPorTick() > 0 && chefe.getAlvos().ticksDesdeUltimoDano() > status.curaAtrasoTicks();
    }
}
