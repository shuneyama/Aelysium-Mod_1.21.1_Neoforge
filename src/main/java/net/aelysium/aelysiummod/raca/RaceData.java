package net.aelysium.aelysiummod.raca;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RaceData extends SavedData {

    private static final String DATA_NAME = "aelysium_race_data";

    private final Map<UUID, RaceType> playerRaces = new HashMap<>();

    public static RaceData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(
                        new Factory<>(RaceData::new, RaceData::load),
                        DATA_NAME
                );
    }

    public RaceType getRace(UUID uuid) {
        return playerRaces.getOrDefault(uuid, RaceType.NONE);
    }

    public void setRace(UUID uuid, RaceType race) {
        playerRaces.put(uuid, race);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag races = new CompoundTag();
        playerRaces.forEach((uuid, race) -> races.putString(uuid.toString(), race.id));
        tag.put("races", races);
        return tag;
    }

    public static RaceData load(CompoundTag tag, HolderLookup.Provider registries) {
        RaceData data = new RaceData();
        CompoundTag races = tag.getCompound("races");
        for (String key : races.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                RaceType race = RaceType.fromId(races.getString(key));
                data.playerRaces.put(uuid, race);
            } catch (IllegalArgumentException ignored) {}
        }
        return data;
    }
}