package net.aelysium.aelysiummod.comandos.chat;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.time.TimeCorGerenciador;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;

public class GlobalChatCommand {

    public static int sendGlobalMessage(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            String message = StringArgumentType.getString(context, "mensagem");

            MutableComponent messageComponent = Component.literal(message);
            MutableComponent finalMessage = buildChatMessage(player, messageComponent);

            context.getSource().getServer().getPlayerList().broadcastSystemMessage(finalMessage, false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Erro ao enviar mensagem global: " + e.getMessage())
            );
            return 0;
        }
    }

    private static MutableComponent buildChatMessage(ServerPlayer player, Component originalMessage) {
        PlayerTeam team = player.getTeam();

        // Se o jogador não está em um time, usa formato padrão
        if (team == null) {
            return Component.literal("<")
                    .append(player.getDisplayName())
                    .append("> ")
                    .append(originalMessage);
        }

        // Verifica se o time tem cor customizada
        return TimeCorGerenciador.getTeamColor(team)
                .map(customColor -> {
                    Component prefix = team.getPlayerPrefix();
                    Component suffix = team.getPlayerSuffix();

                    MutableComponent finalPrefix = Component.literal("");
                    MutableComponent finalSuffix = Component.literal("");

                    // Processa o prefixo com cor customizada se existir
                    if (!prefix.getString().isEmpty()) {
                        TimeCorGerenciador.getPrefixColor(team.getName()).ifPresentOrElse(
                                prefixColor -> finalPrefix.append(
                                        Component.literal(prefix.getString()).setStyle(prefixColor.getStyle())
                                ),
                                () -> finalPrefix.append(prefix)
                        );
                    }

                    // Processa o sufixo com cor customizada se existir
                    if (!suffix.getString().isEmpty()) {
                        TimeCorGerenciador.getSuffixColor(team.getName()).ifPresentOrElse(
                                suffixColor -> finalSuffix.append(
                                        Component.literal(suffix.getString()).setStyle(suffixColor.getStyle())
                                ),
                                () -> finalSuffix.append(suffix)
                        );
                    }

                    // Nome do jogador com estilo do time
                    MutableComponent playerName = Component.literal(player.getGameProfile().getName())
                            .setStyle(customColor.getStyle());

                    // Monta a mensagem final
                    MutableComponent message = Component.literal("<");

                    if (!prefix.getString().isEmpty()) {
                        message.append(finalPrefix).append(" ");
                    }

                    message.append(playerName);

                    if (!suffix.getString().isEmpty()) {
                        message.append(" ").append(finalSuffix);
                    }

                    message.append("> ").append(originalMessage);
                    return message;
                })
                .orElseGet(() ->
                        // Fallback para formato padrão com prefixo/sufixo do time
                        Component.literal("<")
                                .append(team.getPlayerPrefix())
                                .append(player.getDisplayName())
                                .append(team.getPlayerSuffix())
                                .append("> ")
                                .append(originalMessage)
                );
    }
}