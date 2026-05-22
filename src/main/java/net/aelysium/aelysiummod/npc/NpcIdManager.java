package net.aelysium.aelysiummod.npc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.HolderLookup;

public class NpcIdManager extends SavedData {

    private static final String DATA_NAME = "npc_id_counter";
    private int nextId = 1;

    public NpcIdManager() {}

    private NpcIdManager(CompoundTag tag, HolderLookup.Provider provider) {
        nextId = tag.contains("NextId") ? tag.getInt("NextId") : 1;
    }

    public synchronized int nextId() {
        int id = nextId++;
        setDirty();
        return id;
    }

    public static NpcIdManager get(ServerLevel level) {
        ServerLevel overworld = level.getServer()
                .getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworld == null) overworld = level;

        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        NpcIdManager::new,
                        NpcIdManager::load
                ),
                DATA_NAME
        );
    }

    public static NpcIdManager load(CompoundTag tag, HolderLookup.Provider provider) {
        return new NpcIdManager(tag, provider);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("NextId", nextId);
        return tag;
    }
}