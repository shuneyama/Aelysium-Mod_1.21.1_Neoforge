package net.aelysium.aelysiummod.nickname;

import java.util.List;
import net.aelysium.aelysiummod.util.ChatConfig;
import net.aelysium.aelysiummod.habilidade.HabilidadeManager;
import net.aelysium.aelysiummod.network.AelysiumNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

@EventBusSubscriber(modid = "aelysiummod", bus = EventBusSubscriber.Bus.GAME)
public class ChatCor {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        if (HabilidadeManager.estaMudo(player.getUUID())) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cVocê está silenciado e não pode enviar mensagens no chat."));
            return;
        }

        PlayerTeam team = player.getTeam();

        event.setCanceled(true);

        Component originalMessage = event.getMessage();
        List<ServerPlayer> recipients = getRecipients(player);
        MutableComponent finalMessage = buildChatMessage(player, team, originalMessage);

        for (ServerPlayer recipient : recipients) {
            recipient.sendSystemMessage(finalMessage);
        }

        AelysiumNetwork.enviarBalloonParaProximos(player, event.getRawText());
    }

    private static List<ServerPlayer> getRecipients(ServerPlayer sender) {
        if (!ChatConfig.isLocalChatEnabled()) {
            return sender.getServer().getPlayerList().getPlayers();
        }

        double radius = ChatConfig.getLocalChatRadius();
        return sender.getServer().getPlayerList().getPlayers().stream()
                .filter(player -> {
                    if (player.level().dimension() != sender.level().dimension()) {
                        return false;
                    }
                    double distance = player.distanceTo(sender);
                    return distance <= radius;
                })
                .toList();
    }

    private static MutableComponent buildChatMessage(ServerPlayer player, PlayerTeam team, Component originalMessage) {
        if (player.getServer() != null) {
            NicknameData data = NicknameData.get(player.getServer());
            MutableComponent styledName = data.getStyledFullName(player.getUUID());
            if (styledName != null) {
                return Component.literal("<")
                        .append(styledName)
                        .append("> ")
                        .append(originalMessage);
            }
        }

        if (team != null) {
            return Component.literal("<")
                    .append(team.getPlayerPrefix())
                    .append(player.getDisplayName())
                    .append(team.getPlayerSuffix())
                    .append("> ")
                    .append(originalMessage);
        }

        return Component.literal("<")
                .append(player.getDisplayName())
                .append("> ")
                .append(originalMessage);
    }
}