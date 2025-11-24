package net.aelysium.aelysiummod.team;

import net.minecraft.network.chat.Component;
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

            Component playerName = Component.literal(player.getGameProfile().getName())
                    .withColor(customColor.getRgb());

            Component newMessage = Component.literal("<")
                    .append(playerName)
                    .append(Component.literal("> "))
                    .append(originalMessage);

            event.setCanceled(true);

            player.getServer().getPlayerList().broadcastSystemMessage(newMessage, false);
        });
    }
}