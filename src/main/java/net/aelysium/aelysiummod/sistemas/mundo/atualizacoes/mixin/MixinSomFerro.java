package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.aelysium.aelysiummod.sistemas.mundo.flora.SonsExtras;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.class)
public abstract class MixinSomFerro {

    @ModifyReturnValue(method = "getSoundType", at = @At("RETURN"))
    private SoundType aelysium$ferro(SoundType original, @Local(argsOnly = true) BlockState estado) {
        if (estado.is(Blocks.IRON_BLOCK) || estado.is(Blocks.IRON_BARS) || estado.is(Blocks.IRON_TRAPDOOR)
                || estado.is(Blocks.IRON_DOOR) || estado.is(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE)) {
            return SonsExtras.IRON;
        }
        return original;
    }
}
