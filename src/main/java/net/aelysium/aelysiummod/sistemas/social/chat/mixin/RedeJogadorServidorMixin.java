package net.aelysium.aelysiummod.sistemas.social.chat.mixin;

import net.aelysium.aelysiummod.sistemas.social.chat.GerenciadorChat;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class RedeJogadorServidorMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "broadcastChatMessage", at = @At("HEAD"), cancellable = true)
    private void aelysium$interceptChat(PlayerChatMessage message, CallbackInfo ci) {
        if (!AelysiumGuarda.isAutorizado()) return;
        String texto = message.signedContent();

        if (texto.startsWith("/")) return;

        GerenciadorChat.enviarMensagemLocal(player, texto);
        ci.cancel();
    }
}