package net.aelysium.aelysiummod.mixin;

import net.aelysium.aelysiummod.config.ModConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(
            method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onBroadcastSystemMessage(Component message, boolean bypassHiddenChat, CallbackInfo ci) {
        if (!ModConfig.areJoinLeaveMessagesEnabled()) {
            String text = message.getString();

            // Verifica se é uma mensagem de join/leave (inglês e português)
            if (text.contains("joined the game") ||
                    text.contains("left the game") ||
                    text.contains("entrou no jogo") ||
                    text.contains("saiu do jogo")) {
                ci.cancel();
            }
        }
    }
}