package net.aelysium.aelysiummod.nucleo.comando.condicao;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.sistemas.admin.vip.GerenciadorVips;
import net.aelysium.aelysiummod.sistemas.rp.escola.ElementoMagico;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;

public final class CondicoesJogador {

    private static final String JOGADOR = "jogador";
    private static final String VALOR = "valor";

    private static final SuggestionProvider<CommandSourceStack> SUGERIR_RACAS = (ctx, construtor) ->
            SharedSuggestionProvider.suggest(Arrays.stream(TipoRaca.values()).map(raca -> raca.id), construtor);
    private static final SuggestionProvider<CommandSourceStack> SUGERIR_DEUSES = (ctx, construtor) ->
            SharedSuggestionProvider.suggest(Arrays.stream(TipoDeus.values()).map(deus -> deus.id), construtor);
    private static final SuggestionProvider<CommandSourceStack> SUGERIR_ESCOLAS = (ctx, construtor) ->
            SharedSuggestionProvider.suggest(Arrays.stream(ElementoMagico.values()).map(ElementoMagico::id), construtor);
    private static final SuggestionProvider<CommandSourceStack> SUGERIR_VIPS = (ctx, construtor) ->
            SharedSuggestionProvider.suggest(GerenciadorVips.listar(ctx.getSource().getServer()).stream()
                    .map(nome -> nome.contains(" ") ? "\"" + nome + "\"" : nome), construtor);
    private static final SuggestionProvider<CommandSourceStack> SUGERIR_REGIOES = (ctx, construtor) ->
            SharedSuggestionProvider.suggest(GerenciadorRegioes.getInstance().getNomesRegioes(), construtor);

    private CondicoesJogador() {
    }

    @FunctionalInterface
    private interface TesteJogador {
        boolean testar(ServerPlayer jogador, CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> condicoes(CondicoesExecute.Ramo ramo) {
        return Commands.literal(JOGADOR).then(Commands.argument(JOGADOR, EntityArgument.player())
                .then(comValor(ramo, "raca", SUGERIR_RACAS, (j, ctx) -> CriteriosJogador.raca(j, TipoRaca.fromId(valor(ctx)))))
                .then(comValor(ramo, "deus", SUGERIR_DEUSES, (j, ctx) -> CriteriosJogador.deus(j, TipoDeus.fromId(valor(ctx)))))
                .then(comValor(ramo, "escola", SUGERIR_ESCOLAS, (j, ctx) -> CriteriosJogador.escola(j, ElementoMagico.doId(valor(ctx)))))
                .then(comValor(ramo, "vip", SUGERIR_VIPS, (j, ctx) -> CriteriosJogador.vip(j, valor(ctx))))
                .then(comValor(ramo, "regiao", SUGERIR_REGIOES, (j, ctx) -> CriteriosJogador.regiao(j, valor(ctx))))
                .then(ramo.condicao(Commands.literal("live"), ctx -> testar(ctx, (j, c) -> CriteriosJogador.live(j))))
                .then(ramo.condicao(Commands.literal("silenciado"), ctx -> testar(ctx, (j, c) -> CriteriosJogador.silenciado(j)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> comValor(CondicoesExecute.Ramo ramo, String literal,
                                                                       SuggestionProvider<CommandSourceStack> sugestoes, TesteJogador teste) {
        return Commands.literal(literal).then(ramo.condicao(
                Commands.argument(VALOR, StringArgumentType.string()).suggests(sugestoes),
                ctx -> testar(ctx, teste)));
    }

    private static String valor(CommandContext<CommandSourceStack> ctx) {
        return StringArgumentType.getString(ctx, VALOR);
    }

    private static boolean testar(CommandContext<CommandSourceStack> ctx, TesteJogador teste) throws CommandSyntaxException {
        return teste.testar(EntityArgument.getPlayer(ctx, JOGADOR), ctx);
    }
}
