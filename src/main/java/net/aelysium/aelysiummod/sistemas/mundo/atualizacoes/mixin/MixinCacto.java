package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.flora.ModFlora;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CactusBlock.class)
public abstract class MixinCacto {

    @Shadow
    protected abstract boolean canSurvive(BlockState estado, LevelReader mundo, BlockPos pos);

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void aelysium$crescimento(BlockState estado, ServerLevel mundo, BlockPos pos, RandomSource aleatorio, CallbackInfo ci) {
        ci.cancel();
        Block cacto = estado.getBlock();
        BlockPos acima = pos.above();
        if (!mundo.isEmptyBlock(acima)) return;
        int altura = 1;
        int idade = estado.getValue(CactusBlock.AGE);
        while (mundo.getBlockState(pos.below(altura)).is(cacto)) {
            altura++;
            if (altura == 3 && idade == 15) return;
        }
        if (idade == 8 && canSurvive(cacto.defaultBlockState(), mundo, acima)) {
            double chance = altura >= 3 ? 0.25D : 0.1D;
            if (aleatorio.nextDouble() <= chance) {
                mundo.setBlockAndUpdate(acima, ModFlora.FLOR_DE_CACTO.get().defaultBlockState());
            }
        } else if (idade == 15 && altura < 3) {
            mundo.setBlockAndUpdate(acima, cacto.defaultBlockState());
            BlockState novo = estado.setValue(CactusBlock.AGE, 0);
            mundo.setBlock(pos, novo, 260);
            mundo.neighborChanged(novo, acima, cacto, pos, false);
        }
        if (idade < 15) {
            mundo.setBlock(pos, estado.setValue(CactusBlock.AGE, idade + 1), 260);
        }
    }
}
