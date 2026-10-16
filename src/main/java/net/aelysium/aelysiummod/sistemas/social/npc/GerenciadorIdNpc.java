package net.aelysium.aelysiummod.sistemas.social.npc;

import net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.HolderLookup;

public class GerenciadorIdNpc extends DadosEmArquivos {

    private static final String DATA_NAME = "npc_id_counter";
    private int nextId = 1;

    public GerenciadorIdNpc() {}

    private GerenciadorIdNpc(CompoundTag tag, HolderLookup.Provider provider) {
        nextId = tag.contains("NextId") ? tag.getInt("NextId") : 1;
    }

    public synchronized int nextId() {
        int id = nextId++;
        setDirty();
        return id;
    }

    public static GerenciadorIdNpc get(ServerLevel level) {
        return obter(level.getServer(), new SavedData.Factory<>(GerenciadorIdNpc::new, GerenciadorIdNpc::load), DATA_NAME,
                DadosEmArquivos.Destino.global("npcs", "contador_ids.json"));
    }

    public static GerenciadorIdNpc load(CompoundTag tag, HolderLookup.Provider provider) {
        return new GerenciadorIdNpc(tag, provider);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("NextId", nextId);
        return tag;
    }
}