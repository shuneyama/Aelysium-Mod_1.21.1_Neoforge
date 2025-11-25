package net.aelysium.aelysiummod.team;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

/**
 * Handler para aplicar cores customizadas nas mensagens de chat
 */
@EventBusSubscriber(modid = "aelysiummod", bus = EventBusSubscriber.Bus.GAME)
public class ChatCor {

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        PlayerTeam team = player.getTeam();

        if (team == null) return;

        // Verifica se o time tem uma cor customizada
        TimeCorGerenciador.getTeamColor(team).ifPresent(customColor -> {
            // Pega a mensagem original
            Component originalMessage = event.getMessage();

            // Pega o prefixo e sufixo do time
            Component prefix = team.getPlayerPrefix();
            Component suffix = team.getPlayerSuffix();

            // Cria componentes mutáveis para prefixo e sufixo
            MutableComponent finalPrefix = Component.literal("");
            MutableComponent finalSuffix = Component.literal("");

            // Aplica cor customizada ao prefixo se definida
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

            // Aplica cor customizada ao sufixo se definida
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

            // Cria o nome do jogador com a cor e estilo customizados
            MutableComponent playerName = Component.literal(player.getGameProfile().getName())
                    .setStyle(customColor.getStyle());

            // Reconstrói a mensagem de chat mantendo prefixo e sufixo
            MutableComponent newMessage = Component.literal("<");

            // Adiciona prefixo se existir
            if (!prefix.getString().isEmpty()) {
                newMessage.append(finalPrefix).append(" ");
            }

            // Adiciona o nome do jogador
            newMessage.append(playerName);

            // Adiciona sufixo se existir
            if (!suffix.getString().isEmpty()) {
                newMessage.append(" ").append(finalSuffix);
            }

            // Finaliza a mensagem
            newMessage.append(Component.literal("> ")).append(originalMessage);

            // Cancela o evento original
            event.setCanceled(true);

            // Envia a mensagem customizada para todos os jogadores
            player.getServer().getPlayerList().broadcastSystemMessage(newMessage, false);
        });
    }
}