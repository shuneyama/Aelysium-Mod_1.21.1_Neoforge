package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.GerenciadorFiltroReceitas;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoListaBanimentos;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.UtilitariosListaBanimentos;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.DadosBanimentoCliente;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;

public class EventosBanimentoItem {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (event.getEntity().level().isClientSide()) {
            if (DadosBanimentoCliente.isBlockedLocally(event.getItem())) {
                event.setCanceled(true);
            }
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (UtilitariosListaBanimentos.isBlockedForPlayer(event.getItem(), player)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (event.getEntity().level().isClientSide()) {
            if (DadosBanimentoCliente.isBlockedLocally(event.getItem())) {
                event.setCanceled(true);
            }
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (UtilitariosListaBanimentos.isBlockedForPlayer(event.getItem(), player)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (event.getEntity().level().isClientSide()) {
            if (DadosBanimentoCliente.isBlockedLocally(event.getItem())) {
                event.setResultStack(ItemStack.EMPTY);
            }
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (UtilitariosListaBanimentos.isBlockedForPlayer(event.getItem(), player)) {
                    event.setResultStack(ItemStack.EMPTY);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (event.getEntity().level().isClientSide()) {
            if (DadosBanimentoCliente.isBlockedLocally(event.getItemStack())) {
                event.setCanceled(true);
            }
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (UtilitariosListaBanimentos.isBlockedForPlayer(event.getItemStack(), player)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUseOnBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (event.getEntity().level().isClientSide()) {
            if (DadosBanimentoCliente.isBlockedLocally(event.getItemStack())) {
                event.setCanceled(true);
            }
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (UtilitariosListaBanimentos.isBlockedForPlayer(event.getItemStack(), player)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttack(AttackEntityEvent event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (event.getEntity().level().isClientSide()) {
            if (DadosBanimentoCliente.isBlockedLocally(event.getEntity().getMainHandItem())) {
                event.setCanceled(true);
            }
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (UtilitariosListaBanimentos.isBlockedForPlayer(player.getMainHandItem(), player)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockBreak(PlayerEvent.BreakSpeed event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (event.getEntity().level().isClientSide()) {
            if (DadosBanimentoCliente.isBlockedLocally(event.getEntity().getMainHandItem())) {
                event.setNewSpeed(0.0f);
            }
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (UtilitariosListaBanimentos.isBlockedForPlayer(player.getMainHandItem(), player)) {
                    event.setNewSpeed(0.0f);
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemAttributes(net.neoforged.neoforge.event.ItemAttributeModifierEvent event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        if (DadosBanimentoCliente.isBlockedLocally(stack)) {
            event.clearModifiers();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack to = event.getTo();
        if (to.isEmpty()) return;

        EquipmentSlot slot = event.getSlot();
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR && slot != EquipmentSlot.OFFHAND) return;

        if (UtilitariosListaBanimentos.isBlockedForPlayer(to, player)) {
            player.getServer().execute(() -> {
                ItemStack equipped = player.getItemBySlot(slot);
                if (!equipped.isEmpty() && UtilitariosListaBanimentos.isBlockedForPlayer(equipped, player)) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                    if (!player.getInventory().add(equipped)) {
                        player.drop(equipped, false);
                    }
                    player.sendSystemMessage(Component.literal(ConfiguracaoListaBanimentos.getTooltipBanned())
                            .withStyle(ChatFormatting.RED));
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack crafted = event.getCrafting();
        if (UtilitariosListaBanimentos.isBlockedForPlayer(crafted, player)) {
            crafted.setCount(0);
            if (!ConfiguracaoListaBanimentos.isSilentCraftBlock()) {
                player.sendSystemMessage(Component.literal(ConfiguracaoListaBanimentos.getCraftBlockMessage())
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }
        if (GerenciadorFiltroReceitas.isRecipeResultBlocked(crafted)) {
            crafted.setCount(0);
            player.sendSystemMessage(Component.translatable("mensagem.aelysiummod.banimentos.voce_nao_pode_craftar_este_item")
                    .withStyle(ChatFormatting.RED));
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (event.getEntity().level().isClientSide()) return;

        event.getDrops().removeIf(itemEntity -> {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemEntity.getItem().getItem());
            return id != null && ConfiguracaoListaBanimentos.isBanned(id);
        });
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (!ConfiguracaoListaBanimentos.isShowTooltip()) return;

        ItemStack stack = event.getItemStack();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return;

        if (!DadosBanimentoCliente.isBanned(id)) return;

        if (DadosBanimentoCliente.isAllowed(id)) {
            event.getToolTip().add(Component.literal(ConfiguracaoListaBanimentos.getTooltipAllowed())
                    .withStyle(ChatFormatting.GREEN));
        } else {
            event.getToolTip().add(Component.literal(ConfiguracaoListaBanimentos.getTooltipBanned())
                    .withStyle(ChatFormatting.RED));
        }
    }
}