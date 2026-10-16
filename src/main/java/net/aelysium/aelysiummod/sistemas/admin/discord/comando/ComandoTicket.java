package net.aelysium.aelysiummod.sistemas.admin.discord.comando;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.aelysium.aelysiummod.sistemas.admin.discord.ConfiguracaoDiscord;
import net.aelysium.aelysiummod.sistemas.admin.discord.WebhookDiscord;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ComandoTicket {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("ticket")
                .then(Commands.argument("mensagem", StringArgumentType.greedyString())
                        .executes(ComandoTicket::executar)
                );
    }

    private static int executar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer autor = ctx.getSource().getPlayerOrException();
        String mensagem = StringArgumentType.getString(ctx, "mensagem").trim();
        MinecraftServer server = ctx.getSource().getServer();

        if (mensagem.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.discord.voce_precisa_escrever_uma_mensagem"));
            return 0;
        }

        String nomeAutor = autor.getGameProfile().getName();

        Component mensagemAdmins = Component.translatableEscape("mensagem.aelysiummod.discord.ticket", nomeAutor, mensagem);
        int enviados = 0;
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (online.hasPermissions(2)) {
                online.sendSystemMessage(mensagemAdmins);
                enviados++;
            }
        }

        enviarParaDiscord(nomeAutor, mensagem);

        ctx.getSource().sendSuccess(() -> Component.translatable("mensagem.aelysiummod.discord.sua_mensagem_foi_enviada_para_todos_os").withStyle(ChatFormatting.GREEN), false);

        return enviados;
    }

    private static void enviarParaDiscord(String nomeAutor, String mensagem) {
        ConfiguracaoDiscord.Dados cfg = ConfiguracaoDiscord.get();
        if (!cfg.ativo) return;
        if (cfg.webhookTickets == null || cfg.webhookTickets.isBlank()) return;

        String conteudo = cfg.formatoTicket
                .replace("{player}", nomeAutor)
                .replace("{mensagem}", mensagem);

        if (cfg.cargoTicketId != null && !cfg.cargoTicketId.isBlank()) {
            conteudo = "<@&" + cfg.cargoTicketId + "> " + conteudo;
        }

        WebhookDiscord.enviarComMencaoCargo(cfg.webhookTickets, cfg.nomeTickets, cfg.avatarTickets, conteudo, cfg.cargoTicketId);
    }
}
