package net.aelysium.aelysiummod.banlist.data;

import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerBanData extends SavedData {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DATA_NAME = "banlist_players";

    private final Map<UUID, Set<ResourceLocation>> playerAllowedItems = new ConcurrentHashMap<>();

    public PlayerBanData() {
    }

    public static PlayerBanData load(CompoundTag tag, HolderLookup.Provider provider) {
        PlayerBanData data = new PlayerBanData();

        CompoundTag players = tag.getCompound("players");
        for (String uuidStr : players.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Set<ResourceLocation> items = ConcurrentHashMap.newKeySet();
                ListTag list = players.getList(uuidStr, Tag.TAG_STRING);
                for (int i = 0; i < list.size(); i++) {
                    ResourceLocation rl = ResourceLocation.tryParse(list.getString(i));
                    if (rl != null) {
                        items.add(rl);
                    }
                }
                data.playerAllowedItems.put(uuid, items);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("[Banlist] UUID inválido no SavedData: {}", uuidStr);
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag players = new CompoundTag();

        for (Map.Entry<UUID, Set<ResourceLocation>> entry : playerAllowedItems.entrySet()) {
            ListTag list = new ListTag();
            for (ResourceLocation rl : entry.getValue()) {
                list.add(StringTag.valueOf(rl.toString()));
            }
            players.put(entry.getKey().toString(), list);
        }

        tag.put("players", players);
        return tag;
    }

    public boolean isAllowed(UUID playerId, ResourceLocation itemId) {
        Set<ResourceLocation> allowed = playerAllowedItems.get(playerId);
        return allowed != null && allowed.contains(itemId);
    }

    public boolean allowItem(UUID playerId, ResourceLocation itemId) {
        Set<ResourceLocation> items = playerAllowedItems.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet());
        boolean added = items.add(itemId);
        if (added) setDirty();
        return added;
    }

    public boolean revokeItem(UUID playerId, ResourceLocation itemId) {
        Set<ResourceLocation> items = playerAllowedItems.get(playerId);
        if (items == null) return false;
        boolean removed = items.remove(itemId);
        if (removed) {
            if (items.isEmpty()) playerAllowedItems.remove(playerId);
            setDirty();
        }
        return removed;
    }

    public Set<ResourceLocation> getAllowedItems(UUID playerId) {
        Set<ResourceLocation> items = playerAllowedItems.get(playerId);
        return items != null ? Collections.unmodifiableSet(items) : Collections.emptySet();
    }

    public static PlayerBanData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(PlayerBanData::new, PlayerBanData::load),
                DATA_NAME
        );
    }
}