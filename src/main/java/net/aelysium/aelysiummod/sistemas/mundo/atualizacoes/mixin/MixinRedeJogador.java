package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.RegrasAtualizadas;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinRedeJogador {

    @Redirect(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isChangingDimension()Z", ordinal = 0))
    private boolean aelysium$semChecagemMovimento(ServerPlayer jogador) {
        return jogador.isChangingDimension() || jogador.level().getGameRules().getBoolean(RegrasAtualizadas.DESATIVAR_CHECAGEM_MOVIMENTO);
    }
}
