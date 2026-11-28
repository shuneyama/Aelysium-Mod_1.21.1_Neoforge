package net.aelysium.aelysiummod.nucleo.texto.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatFormatting.class)
public abstract class CorAmareloMixin {

    @ModifyReturnValue(method = "getColor", at = @At("RETURN"))
    private Integer aelysium$amarelo(Integer original) {
        return (Object) this == ChatFormatting.YELLOW ? Integer.valueOf(0xFFE265) : original;
    }
}
