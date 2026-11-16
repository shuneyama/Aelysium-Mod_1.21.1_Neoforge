package net.aelysium.aelysiummod.compatibilidade.articulacoes.mixin.animador;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "dev.kosmx.playerAnim.impl.animation.IBendHelper", remap = false)
public interface MixinAjudanteDobra {

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Ldev/kosmx/playerAnim/impl/Helper;isBendEnabled()Z"), require = 1)
    private static boolean cpmec$usarDobraPropria() {
        return false;
    }
}
