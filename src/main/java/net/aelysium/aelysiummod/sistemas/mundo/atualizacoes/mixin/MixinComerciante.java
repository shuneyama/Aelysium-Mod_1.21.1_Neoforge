package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.world.entity.npc.WanderingTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTrader.class)
public abstract class MixinComerciante {

    @Shadow
    protected abstract void experimentalUpdateTrades();

    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void aelysium$rebalanceado(CallbackInfo ci) {
        experimentalUpdateTrades();
        ci.cancel();
    }
}
