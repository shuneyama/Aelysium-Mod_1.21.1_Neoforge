package net.aelysium.aelysiummod.mixin;

import net.aelysium.aelysiummod.chat.ChatManager;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "broadcastChatMessage", at = @At("HEAD"), cancellable = true)
    private void aelysium$interceptChat(PlayerChatMessage message, CallbackInfo ci) {
        String texto = message.signedContent();

        if (texto.startsWith("/")) return;

        ChatManager.enviarMensagemLocal(player, texto);
        ci.cancel();
    }
}