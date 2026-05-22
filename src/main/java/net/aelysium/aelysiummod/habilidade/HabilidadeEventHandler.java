package net.aelysium.aelysiummod.habilidade;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class HabilidadeEventHandler {

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (HabilidadeManager.estaCongelado(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (HabilidadeManager.estaCongelado(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (HabilidadeManager.estaCongelado(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onBreakBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (HabilidadeManager.estaCongelado(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (HabilidadeManager.temVoo(player)) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05f);
                player.onUpdateAbilities();
            }
            if (HabilidadeManager.temDeus(player)) {
                player.setInvulnerable(false);
            }
            if (HabilidadeManager.estaCongelado(player)) {
                ResourceLocation freezeId = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "congelar_movement");
                player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(freezeId);
                player.getAttribute(Attributes.JUMP_STRENGTH).removeModifier(freezeId);
            }
            HabilidadeManager.limpar(player.getUUID());
        }
    }
}
