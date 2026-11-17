package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.flora.SonsExtras;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class MixinSomFerro {

    @Inject(method = "getSoundType", at = @At("HEAD"), cancellable = true)
    private void aelysium$ferro(BlockState estado, CallbackInfoReturnable<SoundType> cir) {
        if (estado.is(Blocks.IRON_BLOCK) || estado.is(Blocks.IRON_BARS) || estado.is(Blocks.IRON_TRAPDOOR)
                || estado.is(Blocks.IRON_DOOR) || estado.is(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE)) {
            cir.setReturnValue(SonsExtras.IRON);
        }
    }
}
