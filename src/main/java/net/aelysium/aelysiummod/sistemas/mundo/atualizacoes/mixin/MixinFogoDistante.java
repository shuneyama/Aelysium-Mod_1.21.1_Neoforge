package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.RegrasAtualizadas;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public abstract class MixinFogoDistante {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void aelysium$longe(BlockState estado, ServerLevel mundo, BlockPos pos, RandomSource aleatorio, CallbackInfo ci) {
        if (!RegrasAtualizadas.fogoPermitido(mundo, pos)) ci.cancel();
    }
}
