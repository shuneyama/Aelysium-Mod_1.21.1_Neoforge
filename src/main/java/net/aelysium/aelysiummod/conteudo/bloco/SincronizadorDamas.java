package net.aelysium.aelysiummod.conteudo.bloco;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModBlocos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class SincronizadorDamas {

    private static final Map<ServerLevel, Set<LevelChunk>> CHUNKS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void aoCarregarChunk(ChunkEvent.Load evento) {
        if (!(evento.getLevel() instanceof ServerLevel nivel) || !(evento.getChunk() instanceof LevelChunk chunk)) return;
        CHUNKS.computeIfAbsent(nivel, n -> ConcurrentHashMap.newKeySet()).add(chunk);
        converter(nivel, chunk, false);
    }

    @SubscribeEvent
    public static void aoDescarregarChunk(ChunkEvent.Unload evento) {
        if (!(evento.getLevel() instanceof ServerLevel nivel) || !(evento.getChunk() instanceof LevelChunk chunk)) return;
        Set<LevelChunk> chunks = CHUNKS.get(nivel);
        if (chunks != null) chunks.remove(chunk);
    }

    @SubscribeEvent
    public static void aoPararServidor(ServerStoppedEvent evento) {
        CHUNKS.clear();
    }

    public static void aoMudarLua(MinecraftServer servidor) {
        for (Map.Entry<ServerLevel, Set<LevelChunk>> entrada : CHUNKS.entrySet()) {
            if (entrada.getKey().getServer() != servidor) continue;
            for (LevelChunk chunk : new ArrayList<>(entrada.getValue())) converter(entrada.getKey(), chunk, true);
        }
    }

    private static void converter(ServerLevel nivel, LevelChunk chunk, boolean viaNivel) {
        boolean alvo = DamaNoite.luaVermelhaAtiva();
        Block bloco = ModBlocos.DAMA_DA_NOITE.get();
        List<BlockPos> posicoes = new ArrayList<>();

        for (int i = 0; i < chunk.getSectionsCount(); i++) {
            LevelChunkSection secao = chunk.getSection(i);
            if (secao.hasOnlyAir() || !secao.maybeHas(estado -> estado.is(bloco))) continue;
            int baseY = chunk.getSectionYFromSectionIndex(i) << 4;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState estado = secao.getBlockState(x, y, z);
                        if (estado.is(bloco) && estado.getValue(DamaNoite.LUA_VERMELHA) != alvo) {
                            posicoes.add(new BlockPos(chunk.getPos().getMinBlockX() + x, baseY + y, chunk.getPos().getMinBlockZ() + z));
                        }
                    }
                }
            }
        }

        for (BlockPos pos : posicoes) {
            BlockState atual = chunk.getBlockState(pos);
            BlockState novo = atual.setValue(DamaNoite.LUA_VERMELHA, alvo);
            if (viaNivel) {
                nivel.setBlock(pos, novo, Block.UPDATE_CLIENTS);
            } else {
                chunk.setBlockState(pos, novo, false);
                chunk.setUnsaved(true);
            }
        }
    }
}
