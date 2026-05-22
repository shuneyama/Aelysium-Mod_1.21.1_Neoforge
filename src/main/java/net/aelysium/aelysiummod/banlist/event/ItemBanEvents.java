package net.aelysium.aelysiummod.banlist.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.aelysium.aelysiummod.banlist.config.BanlistConfig;
import net.aelysium.aelysiummod.banlist.data.BanlistUtil;
import net.aelysium.aelysiummod.banlist.network.ClientBanData;

public class ItemBanEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity().level().isClientSide()) {
            if (ClientBanData.isBlockedLocally(event.getItem())) {
                event.setCanceled(true);
            }
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (BanlistUtil.isBlockedForPlayer(event.getItem(), player)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (event.getEntity().level().isClientSide()) {
            if (ClientBanData.isBlockedLocally(event.getItem())) {
                event.setCanceled(true);
            }
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (BanlistUtil.isBlockedForPlayer(event.getItem(), player)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide()) {
            if (ClientBanData.isBlockedLocally(event.getItem())) {
                event.setResultStack(ItemStack.EMPTY);
            }
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (BanlistUtil.isBlockedForPlayer(event.getItem(), player)) {
                    event.setResultStack(ItemStack.EMPTY);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().level().isClientSide()) {
            if (ClientBanData.isBlockedLocally(event.getItemStack())) {
                event.setCanceled(true);
            }
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (BanlistUtil.isBlockedForPlayer(event.getItemStack(), player)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUseOnBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().level().isClientSide()) {
            if (ClientBanData.isBlockedLocally(event.getItemStack())) {
                event.setCanceled(true);
            }
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (BanlistUtil.isBlockedForPlayer(event.getItemStack(), player)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()) {
            if (ClientBanData.isBlockedLocally(event.getEntity().getMainHandItem())) {
                event.setCanceled(true);
            }
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (BanlistUtil.isBlockedForPlayer(player.getMainHandItem(), player)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockBreak(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().level().isClientSide()) {
            if (ClientBanData.isBlockedLocally(event.getEntity().getMainHandItem())) {
                event.setNewSpeed(0.0f);
            }
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (BanlistUtil.isBlockedForPlayer(player.getMainHandItem(), player)) {
                    event.setNewSpeed(0.0f);
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemAttributes(net.neoforged.neoforge.event.ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        if (ClientBanData.isBlockedLocally(stack)) {
            event.clearModifiers();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack to = event.getTo();
        if (to.isEmpty()) return;

        EquipmentSlot slot = event.getSlot();
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR && slot != EquipmentSlot.OFFHAND) return;

        if (BanlistUtil.isBlockedForPlayer(to, player)) {
            player.getServer().execute(() -> {
                ItemStack equipped = player.getItemBySlot(slot);
                if (!equipped.isEmpty() && BanlistUtil.isBlockedForPlayer(equipped, player)) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                    if (!player.getInventory().add(equipped)) {
                        player.drop(equipped, false);
                    }
                    player.sendSystemMessage(Component.literal(BanlistConfig.getTooltipBanned())
                            .withStyle(ChatFormatting.RED));
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack crafted = event.getCrafting();
        if (BanlistUtil.isBlockedForPlayer(crafted, player)) {
            crafted.setCount(0);

            if (!BanlistConfig.isSilentCraftBlock()) {
                player.sendSystemMessage(Component.literal(BanlistConfig.getCraftBlockMessage())
                        .withStyle(ChatFormatting.RED));
            }
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        event.getDrops().removeIf(itemEntity -> {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemEntity.getItem().getItem());
            return id != null && BanlistConfig.isBanned(id);
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (event.getPlayer().level().isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItemEntity().getItem();
        if (BanlistUtil.isBlockedForPlayer(stack, player)) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!BanlistConfig.isShowTooltip()) return;

        ItemStack stack = event.getItemStack();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return;

        if (!ClientBanData.isBanned(id)) return;

        if (ClientBanData.isAllowed(id)) {
            event.getToolTip().add(Component.literal(BanlistConfig.getTooltipAllowed())
                    .withStyle(ChatFormatting.GREEN));
        } else {
            event.getToolTip().add(Component.literal(BanlistConfig.getTooltipBanned())
                    .withStyle(ChatFormatting.RED));
        }
    }
}