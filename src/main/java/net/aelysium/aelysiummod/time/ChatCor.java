package net.aelysium.aelysiummod.time;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

@EventBusSubscriber(modid = "aelysiummod", bus = EventBusSubscriber.Bus.GAME)
public class ChatCor {

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        PlayerTeam team = player.getTeam();

        if (team == null) return;

        TimeCorGerenciador.getTeamColor(team).ifPresent(customColor -> {
            Component originalMessage = event.getMessage();

            Component prefix = team.getPlayerPrefix();
            Component suffix = team.getPlayerSuffix();

            MutableComponent finalPrefix = Component.literal("");
            MutableComponent finalSuffix = Component.literal("");

            if (!prefix.getString().isEmpty()) {
                TimeCorGerenciador.getPrefixColor(team.getName()).ifPresentOrElse(
                        prefixColor -> {
                            finalPrefix.append(Component.literal(prefix.getString()).setStyle(prefixColor.getStyle()));
                        },
                        () -> {
                            finalPrefix.append(prefix);
                        }
                );
            }

            if (!suffix.getString().isEmpty()) {
                TimeCorGerenciador.getSuffixColor(team.getName()).ifPresentOrElse(
                        suffixColor -> {
                            finalSuffix.append(Component.literal(suffix.getString()).setStyle(suffixColor.getStyle()));
                        },
                        () -> {
                            finalSuffix.append(suffix);
                        }
                );
            }

            MutableComponent playerName = Component.literal(player.getGameProfile().getName())
                    .setStyle(customColor.getStyle());

            MutableComponent newMessage = Component.literal("<");

            if (!prefix.getString().isEmpty()) {
                newMessage.append(finalPrefix).append(" ");
            }

            newMessage.append(playerName);

            if (!suffix.getString().isEmpty()) {
                newMessage.append(" ").append(finalSuffix);
            }

            newMessage.append(Component.literal("> ")).append(originalMessage);

            event.setCanceled(true);

            player.getServer().getPlayerList().broadcastSystemMessage(newMessage, false);
        });
    }
}