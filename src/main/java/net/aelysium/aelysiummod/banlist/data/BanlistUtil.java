package net.aelysium.aelysiummod.banlist.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.aelysium.aelysiummod.banlist.config.BanlistConfig;

import javax.annotation.Nullable;
import java.util.UUID;

public class BanlistUtil {

    public static boolean isItemBanned(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && BanlistConfig.isBanned(id);
    }

    public static boolean isItemBanned(ResourceLocation itemId) {
        return BanlistConfig.isBanned(itemId);
    }

    public static boolean isBlockedForPlayer(ItemStack stack, @Nullable ServerPlayer player) {
        if (stack.isEmpty()) return false;
        if (player == null) return false;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return false;
        if (!BanlistConfig.isBanned(id)) return false;

        MinecraftServer server = player.getServer();
        if (server == null) return true;

        PlayerBanData data = PlayerBanData.get(server);
        return !data.isAllowed(player.getUUID(), id);
    }

    public static boolean isBlockedForPlayer(ResourceLocation itemId, UUID playerId, MinecraftServer server) {
        if (!BanlistConfig.isBanned(itemId)) return false;

        PlayerBanData data = PlayerBanData.get(server);
        return !data.isAllowed(playerId, itemId);
    }

    public static boolean hasIndividualPermission(ItemStack stack, ServerPlayer player) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return false;

        MinecraftServer server = player.getServer();
        if (server == null) return false;

        PlayerBanData data = PlayerBanData.get(server);
        return data.isAllowed(player.getUUID(), id);
    }
}
