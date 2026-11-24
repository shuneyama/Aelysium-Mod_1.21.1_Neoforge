package net.aelysium.aelysiummod.sistemas.rp.deus.mixin;

import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.aelysium.aelysiummod.sistemas.rp.deus.UtilDeus;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class MixinJogadorFome {

    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void aelysium_semFomeParaDeus(float exaustao, CallbackInfo ci) {
        if (UtilDeus.poder((Player) (Object) this, TipoDeus.SEM_FOME)) {
            ci.cancel();
        }
    }
}
