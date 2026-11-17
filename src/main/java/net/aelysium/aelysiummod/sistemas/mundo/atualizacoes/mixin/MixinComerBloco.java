package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.flora.BlocosFlora;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(EatBlockGoal.class)
public abstract class MixinComerBloco {

    @Mutable
    @Shadow
    @Final
    private static Predicate<BlockState> IS_TALL_GRASS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void aelysium$comestiveis(CallbackInfo ci) {
        IS_TALL_GRASS = estado -> estado.is(BlocosFlora.COMESTIVEL_OVELHA);
    }
}
