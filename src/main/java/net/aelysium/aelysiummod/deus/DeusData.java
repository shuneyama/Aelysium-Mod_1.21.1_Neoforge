package net.aelysium.aelysiummod.deus;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeusData extends SavedData {

    private static final String DATA_NAME = "aelysium_deus_data";

    private final Map<UUID, DeusType> playerDeus = new HashMap<>();

    public static DeusData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(
                        new Factory<>(DeusData::new, DeusData::load),
                        DATA_NAME
                );
    }

    public DeusType getDeus(UUID uuid) {
        return playerDeus.getOrDefault(uuid, DeusType.NONE);
    }

    public void setDeus(UUID uuid, DeusType deus) {
        playerDeus.put(uuid, deus);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag deuses = new CompoundTag();
        playerDeus.forEach((uuid, deus) -> deuses.putString(uuid.toString(), deus.id));
        tag.put("deuses", deuses);
        return tag;
    }

    public static DeusData load(CompoundTag tag, HolderLookup.Provider registries) {
        DeusData data = new DeusData();
        CompoundTag deuses = tag.getCompound("deuses");
        for (String key : deuses.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                DeusType deus = DeusType.fromId(deuses.getString(key));
                data.playerDeus.put(uuid, deus);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return data;
    }
}