package net.aelysium.aelysiummod.sistemas.social.apelido.comando;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.aelysium.aelysiummod.nucleo.jogador.TipoInsignia;
import net.aelysium.aelysiummod.sistemas.social.apelido.DadosApelido;
import net.aelysium.aelysiummod.sistemas.social.apelido.Pronome;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;
import java.util.UUID;
import net.aelysium.aelysiummod.nucleo.jogador.ConsultaInsignia;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;
import net.aelysium.aelysiummod.sistemas.social.apelido.rede.RedeApelido;

public class ComandoApelido {

    private static final SuggestionProvider<CommandSourceStack> BADGE_SUGGESTIONS =
            (ctx, builder) -> {
                for (TipoInsignia b : TipoInsignia.values()) builder.suggest(b.id);
                builder.suggest("nenhum");
                builder.suggest("automatico");
                return builder.buildFuture();
            };

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("nick")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("jogador", EntityArgument.player())
                        .executes(ComandoApelido::executeEditor)
                        .then(Commands.literal("modificar")
                                .then(Commands.argument("nome", StringArgumentType.greedyString())
                                        .executes(ComandoApelido::executeModificar)
                                )
                        )
                        .then(Commands.literal("resetar")
                                .executes(ComandoApelido::executeResetar)
                        )
                        .then(Commands.literal("prefixo")
                                .then(Commands.argument("badge", StringArgumentType.word())
                                        .suggests(BADGE_SUGGESTIONS)
                                        .executes(ComandoApelido::executePrefixo)
                                )
                        )
                );
    }

    private static int executePrefixo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
        String raw = StringArgumentType.getString(ctx, "badge").toLowerCase(Locale.ROOT);
        MinecraftServer server = ctx.getSource().getServer();
        Component feedback;

        if (raw.equals("automatico")) {
            PerfilJogador.insignia().limparSubstituicao(server, target.getUUID());
            feedback = Component.translatableEscape("mensagem.aelysiummod.apelido.badge_automatico", target.getName().getString());
        } else if (raw.equals("nenhum")) {
            PerfilJogador.insignia().definirSubstituicao(server, target.getUUID(), ConsultaInsignia.NENHUMA);
            feedback = Component.translatableEscape("mensagem.aelysiummod.apelido.badge_removido", target.getName().getString());
        } else if (PerfilJogador.insignia().idPersonalizadoValido(raw)) {
            PerfilJogador.insignia().definirSubstituicao(server, target.getUUID(), raw);
            feedback = Component.translatableEscape("mensagem.aelysiummod.apelido.badge_definido", target.getName().getString(), raw);
        } else {
            TipoInsignia badge = TipoInsignia.fromId(raw);
            if (badge == null) {
                ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.apelido.badge_invalido", raw));
                return 0;
            }
            PerfilJogador.insignia().definirSubstituicao(server, target.getUUID(), badge.id);
            feedback = Component.translatableEscape("mensagem.aelysiummod.apelido.badge_definido", target.getName().getString(), badge.id);
        }

        refreshTabList(target);
        Component finalFeedback = feedback;
        ctx.getSource().sendSuccess(() -> finalFeedback, true);
        return 1;
    }

    private static int executeModificar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
        String rawNick = StringArgumentType.getString(ctx, "nome").trim();

        if (rawNick.startsWith("\"") && rawNick.endsWith("\"") && rawNick.length() > 1) {
            rawNick = rawNick.substring(1, rawNick.length() - 1).trim();
        } else if (rawNick.startsWith("\"")) {
            rawNick = rawNick.substring(1).trim();
        }

        if (rawNick.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.apelido.o_nickname_nao_pode_ser_vazio"));
            return 0;
        }

        if (rawNick.chars().anyMatch(Character::isWhitespace)) {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.apelido.sem_espacos"));
            return 0;
        }

        if (rawNick.length() > 24) {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.apelido.o_nickname_nao_pode_ter_mais_de_24"));
            return 0;
        }

        MinecraftServer server = ctx.getSource().getServer();
        DadosApelido data = DadosApelido.get(server);

        UUID previousOwner = data.releasePreviousOwner(rawNick, target.getUUID());
        if (previousOwner != null) {
            data.syncToAll(server, previousOwner);
            ServerPlayer previousPlayer = server.getPlayerList().getPlayer(previousOwner);
            if (previousPlayer != null) refreshTabList(previousPlayer);
        }

        data.setNickSimple(target.getUUID(), rawNick);
        data.syncToAll(server, target.getUUID());
        refreshTabList(target);
        refreshCommandTree(server);

        MutableComponent styled = data.getStyledFullName(target.getUUID());
        MutableComponent feedback = Component.translatableEscape("mensagem.aelysiummod.apelido.nickname_de_alterado_para", target.getName().getString());
        if (styled != null) feedback.append(styled);
        feedback.append(Component.literal("§a."));
        ctx.getSource().sendSuccess(() -> feedback, true);
        return 1;
    }

    private static int executeResetar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
        DadosApelido data = DadosApelido.get(ctx.getSource().getServer());

        if (!data.hasNickname(target.getUUID())) {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.apelido.nao_possui_um_nickname", target.getName().getString()));
            return 0;
        }

        String oldNick = data.getNickname(target.getUUID()).nick();
        data.removeNickname(target.getUUID());
        data.syncToAll(ctx.getSource().getServer(), target.getUUID());
        refreshTabList(target);
        refreshCommandTree(ctx.getSource().getServer());

        ctx.getSource().sendSuccess(
                () -> Component.translatableEscape("mensagem.aelysiummod.apelido.nickname_removido_de", oldNick, target.getName().getString()),
                true
        );
        return 1;
    }

    private static int executeEditor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");

        ServerPlayer executor = ctx.getSource().getPlayerOrException();

        DadosApelido data = DadosApelido.get(ctx.getSource().getServer());
        DadosApelido.EntradaApelido entry = data.getNickname(target.getUUID());

        String badgeOverride = PerfilJogador.insignia().substituicao(ctx.getSource().getServer(), target.getUUID());
        if (badgeOverride == null) badgeOverride = ConsultaInsignia.AUTOMATICA;

        RedeApelido.PacoteAbrirEditorApelido packet;
        if (entry != null) {
            packet = new RedeApelido.PacoteAbrirEditorApelido(
                    target.getUUID(),
                    target.getGameProfile().getName(),
                    entry.prefix(), entry.prefixCor1(), entry.prefixCor2(), entry.prefixFormat(),
                    entry.nick(), entry.nickCor(), entry.nickFormat(),
                    entry.suffix(), entry.suffixCor1(), entry.suffixCor2(), entry.suffixFormat(),
                    badgeOverride, data.getPronomes(target.getUUID())
            );
        } else {
            packet = new RedeApelido.PacoteAbrirEditorApelido(
                    target.getUUID(),
                    target.getGameProfile().getName(),
                    "", 0xFFFFFF, -1, 0,
                    target.getGameProfile().getName(), 0xFFFFFF, 0,
                    "", 0xFFFFFF, -1, 0,
                    badgeOverride, data.getPronomes(target.getUUID())
            );
        }

        PacketDistributor.sendToPlayer(executor, packet);
        return 1;
    }

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildPronomes() {
        com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> adicionar = Commands.literal("adicionar");
        for (Pronome pronome : Pronome.values()) {
            adicionar.then(Commands.literal(pronome.comando).executes(ctx -> {
                ServerPlayer jogador = ctx.getSource().getPlayerOrException();
                DadosApelido.get(ctx.getSource().getServer()).setPronomes(jogador.getUUID(), pronome.id);
                refreshTabList(jogador);
                ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.apelido.pronomes_definidos", pronome.texto), false);
                return 1;
            }));
        }
        return Commands.literal("pronomes")
                .then(Commands.literal("limpar").executes(ctx -> {
                    ServerPlayer jogador = ctx.getSource().getPlayerOrException();
                    DadosApelido.get(ctx.getSource().getServer()).setPronomes(jogador.getUUID(), "");
                    refreshTabList(jogador);
                    ctx.getSource().sendSuccess(() -> Component.translatable("mensagem.aelysiummod.apelido.pronomes_removidos"), false);
                    return 1;
                }))
                .then(adicionar);
    }

    public static void refreshCommandTree(net.minecraft.server.MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            server.getCommands().sendCommands(player);
        }
    }

    public static void refreshTabList(ServerPlayer target) {
        target.refreshDisplayName();
        target.refreshTabListName();
    }
}