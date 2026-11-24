package net.aelysium.aelysiummod.sistemas.rp.skin.mixin;

import net.aelysium.aelysiummod.sistemas.rp.skin.cliente.SkinsJogadoresCliente;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinSkinJogador {

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void aelysium$skinRp(CallbackInfoReturnable<PlayerSkin> retorno) {
        AbstractClientPlayer jogador = (AbstractClientPlayer) (Object) this;
        retorno.setReturnValue(SkinsJogadoresCliente.sobrescrever(jogador.getUUID(), retorno.getReturnValue()));
    }
}
