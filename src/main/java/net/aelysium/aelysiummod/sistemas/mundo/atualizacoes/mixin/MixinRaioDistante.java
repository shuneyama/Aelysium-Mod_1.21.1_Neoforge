package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLevel.class)
public abstract class MixinRaioDistante {

    private boolean aelysium$perto = true;

    @org.spongepowered.asm.mixin.injection.Inject(method = "tickChunk", at = @At("HEAD"))
    private void aelysium$inicio(LevelChunk chunk, int velocidade, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        ServerLevel mundo = (ServerLevel) (Object) this;
        aelysium$perto = mundo.getChunkSource().chunkMap.anyPlayerCloseEnoughForSpawning(chunk.getPos());
    }

    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;isThundering()Z"))
    private boolean aelysium$trovao(ServerLevel mundo) {
        return mundo.isThundering() && aelysium$perto;
    }
}
