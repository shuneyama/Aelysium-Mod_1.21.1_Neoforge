package net.aelysium.aelysiummod.sistemas.social.chat.comando;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.nucleo.comando.TextoComando;
import net.aelysium.aelysiummod.nucleo.configuracao.ConfiguracaoChat;
import net.aelysium.aelysiummod.nucleo.configuracao.ConfiguracaoMod;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.sistemas.social.chat.GerenciadorChat;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public final class ComandoChat {
    private static final String P = "mensagem.aelysiummod.chat_comando.";

    private ComandoChat() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("chat")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status").executes(ComandoChat::status))
                .then(Commands.literal("raio")
                        .then(Commands.literal("padrao").executes(ComandoChat::raioPadrao))
                        .then(Commands.argument("blocos", IntegerArgumentType.integer(1, 1000)).executes(ComandoChat::raio)))
                .then(Commands.literal("entrada")
                        .then(Commands.literal("ligar").executes(ctx -> entrada(ctx, true)))
                        .then(Commands.literal("desligar").executes(ctx -> entrada(ctx, false))))
                .then(Commands.literal("antispam")
                        .then(Commands.argument("segundos", IntegerArgumentType.integer(0, 300)).executes(ComandoChat::antiSpam)));
    }

    public static void registrarAtalhos(CommandDispatcher<CommandSourceStack> dispatcher) {
        atalho(dispatcher, "g", GerenciadorChat::enviarMensagemGlobal);
        atalho(dispatcher, "s", GerenciadorChat::enviarMensagemMundo);
        atalho(dispatcher, "global", GerenciadorChat::enviarMensagemMundo);
    }

    private static void atalho(CommandDispatcher<CommandSourceStack> dispatcher, String nome, BiConsumer<ServerPlayer, String> envio) {
        dispatcher.register(Commands.literal(nome)
                .requires(source -> AelysiumGuarda.isAutorizado())
                .then(Commands.argument("mensagem", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            if (ctx.getSource().getEntity() instanceof ServerPlayer jogador) {
                                envio.accept(jogador, StringArgumentType.getString(ctx, "mensagem"));
                                return 1;
                            }
                            return 0;
                        })));
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack fonte = ctx.getSource();
        fonte.sendSuccess(() -> Component.translatable(P + "status_titulo"), false);
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "status_raio", ChatFormatting.GRAY, ConfiguracaoChat.getLocalChatRadius()), false);
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "status_entrada", ChatFormatting.GRAY,
                estado(ConfiguracaoMod.areJoinLeaveMessagesEnabled())), false);
        int segundos = ConfiguracaoChat.getAntiSpamSegundos();
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "status_antispam", ChatFormatting.GRAY,
                segundos == 0 ? estado(false) : Component.literal(segundos + "s").withStyle(ChatFormatting.YELLOW)), false);
        return 1;
    }

    private static int raio(CommandContext<CommandSourceStack> ctx) {
        int blocos = IntegerArgumentType.getInteger(ctx, "blocos");
        ConfiguracaoChat.setLocalChatRadius(blocos);
        ctx.getSource().sendSuccess(() -> TextoComando.traduzir(P + "raio_definido", ChatFormatting.GREEN, blocos), true);
        return 1;
    }

    private static int raioPadrao(CommandContext<CommandSourceStack> ctx) {
        ConfiguracaoChat.setLocalChatRadius(ConfiguracaoChat.RAIO_PADRAO);
        ctx.getSource().sendSuccess(() -> TextoComando.traduzir(P + "raio_padrao", ChatFormatting.GREEN, ConfiguracaoChat.RAIO_PADRAO), true);
        return 1;
    }

    private static int entrada(CommandContext<CommandSourceStack> ctx, boolean ligar) {
        ConfiguracaoMod.setJoinLeaveMessagesEnabled(ligar);
        ctx.getSource().sendSuccess(() -> Component.translatable(P + (ligar ? "entrada_ligada" : "entrada_desligada")), true);
        return 1;
    }

    private static int antiSpam(CommandContext<CommandSourceStack> ctx) {
        int segundos = IntegerArgumentType.getInteger(ctx, "segundos");
        ConfiguracaoChat.setAntiSpamSegundos(segundos);
        if (segundos == 0) {
            ctx.getSource().sendSuccess(() -> Component.translatable(P + "antispam_desligado"), true);
        } else {
            ctx.getSource().sendSuccess(() -> TextoComando.traduzir(P + "antispam_definido", ChatFormatting.GREEN, segundos), true);
        }
        return 1;
    }

    private static Component estado(boolean ligado) {
        return Component.translatable(ligado ? "mensagem.aelysiummod.comum.ativado" : "mensagem.aelysiummod.comum.desativado");
    }
}
