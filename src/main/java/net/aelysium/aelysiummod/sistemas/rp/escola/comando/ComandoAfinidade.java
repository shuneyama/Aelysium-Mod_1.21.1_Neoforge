package net.aelysium.aelysiummod.sistemas.rp.escola.comando;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.aelysium.aelysiummod.sistemas.rp.escola.ElementoMagico;
import net.aelysium.aelysiummod.sistemas.rp.escola.EscolaSorteio;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class ComandoAfinidade {
    private static final String JOGADORES = "jogadores";
    private static final String P = "mensagem.aelysiummod.escola.";
    private static final ElementoMagico[] ORDEM = EscolaSorteio.ORDEM_PORCENTAGEM;

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("afinidade")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument(JOGADORES, EntityArgument.players())
                        .then(Commands.literal("escola")
                                .executes(ComandoAfinidade::porEscola))
                        .then(Commands.literal("porcentagem")
                                .then(cadeiaPorcentagem())));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> cadeiaPorcentagem() {
        ArgumentBuilder<CommandSourceStack, ?> atual = null;
        for (int i = ORDEM.length - 1; i >= 0; i--) {
            var argumento = Commands.argument(nome(ORDEM[i]), IntegerArgumentType.integer(0, 100));
            if (atual == null) {
                argumento.executes(ComandoAfinidade::porPorcentagem);
            } else {
                argumento.then(atual);
            }
            atual = argumento;
        }
        return atual;
    }

    private static String nome(ElementoMagico elemento) {
        return elemento.name().toLowerCase();
    }

    private static int porEscola(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> alvos = EntityArgument.getPlayers(ctx, JOGADORES);
        int sucesso = 0;
        for (ServerPlayer alvo : alvos) {
            TipoRaca raca = PerfilJogador.raca().raca(alvo.getUUID());
            if (raca == TipoRaca.NONE) {
                ctx.getSource().sendFailure(Component.translatableEscape(P + "nao_tem_uma_raca_definida", alvo.getName().getString()));
                continue;
            }
            EscolaSorteio.Resultado resultado = EscolaSorteio.sortear(raca);
            String nome = alvo.getName().getString();
            ctx.getSource().sendSuccess(() -> Component.translatableEscape(P + "jogador_tem_aptidao_magica_em", nome, resultado.textoColorido()), true);
            sucesso++;
        }
        return sucesso;
    }

    private static int porPorcentagem(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        int[] pesos = new int[ORDEM.length];
        int soma = 0;
        for (int i = 0; i < ORDEM.length; i++) {
            pesos[i] = IntegerArgumentType.getInteger(ctx, nome(ORDEM[i]));
            soma += pesos[i];
        }
        if (soma != 100) {
            ctx.getSource().sendFailure(Component.translatableEscape(P + "porcentagem_soma_invalida", soma));
            return 0;
        }
        Collection<ServerPlayer> alvos = EntityArgument.getPlayers(ctx, JOGADORES);
        for (ServerPlayer alvo : alvos) {
            int d100 = EscolaSorteio.rolarD100();
            EscolaSorteio.Resultado resultado = EscolaSorteio.sortearPorPesos(pesos, d100);
            String nome = alvo.getName().getString();
            ctx.getSource().sendSuccess(() -> Component.translatableEscape(P + "jogador_tem_aptidao_magica_rolagem", nome, resultado.textoColorido(), d100), true);
        }
        return alvos.size();
    }
}
