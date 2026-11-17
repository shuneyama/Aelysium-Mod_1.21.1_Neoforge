package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
public abstract class MixinNeveEmPo {

    @Inject(method = "canEntityWalkOnPowderSnow", at = @At("RETURN"), cancellable = true)
    private static void aelysium$queda(Entity entidade, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && entidade.fallDistance > 2.5F) cir.setReturnValue(false);
    }
}
