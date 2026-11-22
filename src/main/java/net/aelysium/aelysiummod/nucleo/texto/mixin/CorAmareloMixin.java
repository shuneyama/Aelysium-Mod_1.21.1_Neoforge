package net.aelysium.aelysiummod.nucleo.texto.mixin;

import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatFormatting.class)
public abstract class CorAmareloMixin {

    @Inject(method = "getColor", at = @At("RETURN"), cancellable = true)
    private void aelysium$amarelo(CallbackInfoReturnable<Integer> cir) {
        if (((Enum<?>) (Object) this).name().equals("YELLOW")) {
            cir.setReturnValue(0xFFE265);
        }
    }
}
