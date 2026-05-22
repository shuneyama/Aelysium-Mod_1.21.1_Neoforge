package net.aelysium.aelysiummod.mixin;

import net.aelysium.aelysiummod.socialspy.SocialSpyManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(MsgCommand.class)
public class PrivateMessageMixin {

    @Inject(
        method = "sendMessage",
        at = @At("HEAD")
    )
    private static void aelysium$interceptPrivateMessage(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            PlayerChatMessage message,
            CallbackInfo ci) {

        if (!(source.getEntity() instanceof ServerPlayer remetente)) {
            return;
        }

        String mensagem = message.signedContent();

        for (ServerPlayer alvo : targets) {
            SocialSpyManager.enviarParaSocialspyPrivado(remetente, alvo, mensagem);
        }
    }
}
