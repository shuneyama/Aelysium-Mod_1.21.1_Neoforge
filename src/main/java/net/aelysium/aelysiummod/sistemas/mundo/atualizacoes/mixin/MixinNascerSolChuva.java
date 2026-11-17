package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionSpecialEffects.class)
public abstract class MixinNascerSolChuva {

    @Inject(method = "getSunriseColor", at = @At("RETURN"))
    private void aelysium$chuva(float tempo, float parcial, CallbackInfoReturnable<float[]> cir) {
        float[] cor = cir.getReturnValue();
        if (cor == null || Minecraft.getInstance().level == null) return;
        float chuva = Minecraft.getInstance().level.getRainLevel(parcial);
        if (chuva > 0.0F) cor[3] *= 1.0F - chuva;
    }
}
