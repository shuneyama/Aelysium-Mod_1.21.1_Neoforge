package net.aelysium.aelysiummod.sistemas.rp.deus.comando;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.aelysium.aelysiummod.nucleo.comando.TextoComando;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.aelysium.aelysiummod.sistemas.rp.deus.GerenciadorDeuses;
import net.aelysium.aelysiummod.sistemas.rp.deus.DadosDeus;
import net.aelysium.aelysiummod.sistemas.rp.deus.FormaDivina;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ComandoDeus {
    private static final String P = "mensagem.aelysiummod.deus.";

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("deus")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("definir")
                        .then(Commands.argument("deus", StringArgumentType.word())
                                .suggests((ctx, construtor) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(TipoDeus.values()).filter(d -> d != TipoDeus.NONE).map(d -> d.id), construtor))
                                .executes(ctx -> definir(ctx, null))
                                .then(Commands.argument("jogador", EntityArgument.player())
                                        .executes(ctx -> definir(ctx, EntityArgument.getPlayer(ctx, "jogador"))))))
                .then(Commands.literal("remover")
                        .executes(ctx -> remover(ctx, null))
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> remover(ctx, EntityArgument.getPlayer(ctx, "jogador")))))
                .then(Commands.literal("status")
                        .executes(ctx -> status(ctx, null))
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> status(ctx, EntityArgument.getPlayer(ctx, "jogador")))))
                .then(Commands.literal("listar")
                        .executes(ComandoDeus::listar));
    }

    private static ServerPlayer alvo(CommandContext<CommandSourceStack> ctx, ServerPlayer informado) {
        if (informado != null) return informado;
        if (ctx.getSource().getEntity() instanceof ServerPlayer executor) return executor;
        ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.nucleo_comando.apenas_jogadores_podem_usar_este"));
        return null;
    }

    private static int definir(CommandContext<CommandSourceStack> ctx, ServerPlayer informado) throws CommandSyntaxException {
        ServerPlayer jogador = alvo(ctx, informado);
        if (jogador == null) return 0;
        String id = StringArgumentType.getString(ctx, "deus");
        TipoDeus deus = TipoDeus.fromId(id);
        if (deus == TipoDeus.NONE) {
            String validos = Arrays.stream(TipoDeus.values()).filter(d -> d != TipoDeus.NONE).map(d -> d.id).collect(Collectors.joining(", "));
            ctx.getSource().sendFailure(TextoComando.traduzir(P + "deus_invalido", ChatFormatting.RED, id, validos));
            return 0;
        }
        GerenciadorDeuses.definir(jogador, deus);
        String nome = jogador.getName().getString();
        ctx.getSource().sendSuccess(() -> TextoComando.traduzir(P + "aelysium_deus_de_definido_como", ChatFormatting.WHITE, nome, deus.displayName), false);
        return 1;
    }

    private static int remover(CommandContext<CommandSourceStack> ctx, ServerPlayer informado) {
        ServerPlayer jogador = alvo(ctx, informado);
        if (jogador == null) return 0;
        String nome = jogador.getName().getString();
        if (!GerenciadorDeuses.remover(jogador)) {
            ctx.getSource().sendFailure(TextoComando.traduzir(P + "nao_e_deus", ChatFormatting.RED, nome));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> TextoComando.traduzir(P + "aelysium_deus_de_removido", ChatFormatting.WHITE, nome), false);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx, ServerPlayer informado) {
        ServerPlayer jogador = alvo(ctx, informado);
        if (jogador == null) return 0;
        String nome = jogador.getName().getString();
        TipoDeus deus = DadosDeus.get(jogador.serverLevel()).getDeus(jogador.getUUID());
        if (deus == TipoDeus.NONE) {
            ctx.getSource().sendSuccess(() -> TextoComando.traduzir(P + "nao_e_deus", ChatFormatting.GRAY, nome), false);
            return 1;
        }
        boolean forma = FormaDivina.estaAtiva(jogador);
        ctx.getSource().sendSuccess(() -> TextoComando.traduzir(P + "status_deus", ChatFormatting.GRAY, nome, deus.displayName), false);
        ctx.getSource().sendSuccess(() -> TextoComando.traduzir(P + (forma ? "status_forma_ativa" : "status_forma_inativa"), ChatFormatting.GRAY), false);
        return 1;
    }

    private static int listar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack fonte = ctx.getSource();
        ServerPlayer referencia = fonte.getEntity() instanceof ServerPlayer p ? p : fonte.getServer().getPlayerList().getPlayers().stream().findFirst().orElse(null);
        if (referencia == null) {
            fonte.sendSuccess(() -> Component.translatable(P + "listar_vazio"), false);
            return 0;
        }
        Map<UUID, TipoDeus> todos = DadosDeus.get(referencia.serverLevel()).todos();
        todos.values().removeIf(d -> d == TipoDeus.NONE);
        if (todos.isEmpty()) {
            fonte.sendSuccess(() -> Component.translatable(P + "listar_vazio"), false);
            return 1;
        }
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "listar_titulo", ChatFormatting.GOLD, Component.literal(String.valueOf(todos.size()))), false);
        for (Map.Entry<UUID, TipoDeus> entrada : todos.entrySet()) {
            String nome = fonte.getServer().getProfileCache().get(entrada.getKey())
                    .map(perfil -> perfil.getName())
                    .orElse(entrada.getKey().toString());
            fonte.sendSuccess(() -> TextoComando.traduzir(P + "listar_linha", ChatFormatting.GRAY, nome, entrada.getValue().displayName), false);
        }
        return todos.size();
    }
}
