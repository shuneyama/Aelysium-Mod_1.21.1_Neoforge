package net.aelysium.aelysiummod.banlist.network;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientBanData {

    private static final Set<ResourceLocation> BANNED_ITEMS = ConcurrentHashMap.newKeySet();
    private static final Set<ResourceLocation> ALLOWED_ITEMS = ConcurrentHashMap.newKeySet();

    public static void setBannedItems(Set<ResourceLocation> items) {
        BANNED_ITEMS.clear();
        BANNED_ITEMS.addAll(items);
    }

    public static void setAllowedItems(Set<ResourceLocation> items) {
        ALLOWED_ITEMS.clear();
        ALLOWED_ITEMS.addAll(items);
    }

    public static void clear() {
        BANNED_ITEMS.clear();
        ALLOWED_ITEMS.clear();
    }

    public static boolean isBanned(ResourceLocation itemId) {
        return BANNED_ITEMS.contains(itemId);
    }

    public static boolean isAllowed(ResourceLocation itemId) {
        return ALLOWED_ITEMS.contains(itemId);
    }

    public static boolean isBlockedLocally(ResourceLocation itemId) {
        return BANNED_ITEMS.contains(itemId) && !ALLOWED_ITEMS.contains(itemId);
    }

    public static boolean isBlockedLocally(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && isBlockedLocally(id);
    }

    public static Set<ResourceLocation> getBannedItems() {
        return Collections.unmodifiableSet(BANNED_ITEMS);
    }
}
