package net.aelysium.aelysiummod.sistemas.social.maozinha.mixin;

import net.aelysium.aelysiummod.sistemas.social.maozinha.FormacaoMaozinha;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class JogadorPassageiroMaozinhaMixin {

    @Inject(method = "wantsToStopRiding()Z", at = @At("HEAD"), cancellable = true)
    private void aelysium_naoSoltarAoAgachar(CallbackInfoReturnable<Boolean> cir) {
        if (FormacaoMaozinha.ehSeguidor((Player) (Object) this)) cir.setReturnValue(false);
    }

    public boolean shouldRiderSit() {
        return !FormacaoMaozinha.ehLiderComSeguidor((Player) (Object) this);
    }

    public boolean showVehicleHealth() {
        return !FormacaoMaozinha.ehLiderComSeguidor((Player) (Object) this);
    }
}
