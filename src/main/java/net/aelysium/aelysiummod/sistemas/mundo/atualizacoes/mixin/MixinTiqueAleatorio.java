package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkCache.class)
public abstract class MixinTiqueAleatorio {

    @Shadow
    @Final
    ServerLevel level;

    @Shadow
    @Final
    public ChunkMap chunkMap;

    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;anyPlayerCloseEnoughForSpawning(Lnet/minecraft/world/level/ChunkPos;)Z"))
    private boolean aelysium$simulacao(ChunkMap mapa, ChunkPos pos) {
        return mapa.anyPlayerCloseEnoughForSpawning(pos) || level.shouldTickBlocksAt(pos.toLong());
    }

    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner;spawnForChunk(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/NaturalSpawner$SpawnState;ZZZ)V"))
    private void aelysium$spawnPerto(ServerLevel mundo, LevelChunk chunk, NaturalSpawner.SpawnState estado, boolean amigaveis, boolean inimigos, boolean tempo) {
        if (chunkMap.anyPlayerCloseEnoughForSpawning(chunk.getPos())) {
            NaturalSpawner.spawnForChunk(mundo, chunk, estado, amigaveis, inimigos, tempo);
        }
    }
}
