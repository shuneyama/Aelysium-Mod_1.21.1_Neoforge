package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.RegrasAtualizadas;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LavaFluid.class)
public abstract class MixinLavaDistante {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void aelysium$longe(Level mundo, BlockPos pos, FluidState estado, RandomSource aleatorio, CallbackInfo ci) {
        if (mundo instanceof ServerLevel servidor && !RegrasAtualizadas.fogoPermitido(servidor, pos)) ci.cancel();
    }
}
