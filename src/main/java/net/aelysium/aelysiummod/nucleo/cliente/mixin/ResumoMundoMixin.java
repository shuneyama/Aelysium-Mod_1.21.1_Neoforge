package net.aelysium.aelysiummod.nucleo.cliente.mixin;

import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LevelSummary.class, priority = 1001)
public class ResumoMundoMixin {
    @Inject(method = "isExperimental", at = @At(value = "RETURN"), cancellable = true)
    public void isExperimental(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}