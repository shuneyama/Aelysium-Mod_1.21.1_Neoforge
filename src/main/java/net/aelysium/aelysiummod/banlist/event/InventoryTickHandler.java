package net.aelysium.aelysiummod.banlist.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.aelysium.aelysiummod.banlist.config.BanlistConfig;
import net.aelysium.aelysiummod.banlist.data.BanlistUtil;

public class InventoryTickHandler {

    private static int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;

        net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR && slot != EquipmentSlot.OFFHAND) continue;

                ItemStack equipped = player.getItemBySlot(slot);
                if (!equipped.isEmpty() && BanlistUtil.isBlockedForPlayer(equipped, player)) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                    if (!player.getInventory().add(equipped)) {
                        player.drop(equipped, false);
                    }
                    player.sendSystemMessage(Component.literal(BanlistConfig.getTooltipBanned())
                            .withStyle(ChatFormatting.RED));
                }
            }
        }
    }
}
