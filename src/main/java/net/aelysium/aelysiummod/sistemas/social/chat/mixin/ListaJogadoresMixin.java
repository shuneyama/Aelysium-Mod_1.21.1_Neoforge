package net.aelysium.aelysiummod.sistemas.social.chat.mixin;

import net.aelysium.aelysiummod.nucleo.configuracao.ConfiguracaoMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class ListaJogadoresMixin {

    @Inject(
            method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onBroadcastSystemMessage(Component message, boolean bypassHiddenChat, CallbackInfo ci) {
        if (!ConfiguracaoMod.areJoinLeaveMessagesEnabled() && isJoinOrLeaveMessage(message)) {
            ci.cancel();
        }
    }

    private static boolean isJoinOrLeaveMessage(Component message) {
        if (message.getContents() instanceof TranslatableContents translatable) {
            String key = translatable.getKey();
            if (key.equals("multiplayer.player.joined")
                    || key.equals("multiplayer.player.joined.renamed")
                    || key.equals("multiplayer.player.left")
                    || key.equals("multiplayer.player.left.renamed")) {
                return true;
            }
        }

        String text = message.getString();
        return text.contains("joined the game") ||
                text.contains("left the game") ||
                text.contains("entrou no jogo") ||
                text.contains("saiu do jogo");
    }
}