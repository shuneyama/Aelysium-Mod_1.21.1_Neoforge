package net.aelysium.aelysiummod.sistemas.rp.escola.comando;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.aelysium.aelysiummod.sistemas.rp.escola.EscolaSorteio;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;

public class ComandoEscola {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("escola")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("jogadores", EntityArgument.players())
                        .executes(ComandoEscola::executar));
    }

    private static int executar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> alvos = EntityArgument.getPlayers(ctx, "jogadores");
        int sucesso = 0;

        for (ServerPlayer alvo : alvos) {
            TipoRaca raca = PerfilJogador.raca().raca(alvo.getUUID());

            if (raca == TipoRaca.NONE) {
                ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.escola.nao_tem_uma_raca_definida", alvo.getName().getString()));
                continue;
            }

            EscolaSorteio.Resultado resultado = EscolaSorteio.sortear(raca);
            String nome = alvo.getName().getString();

            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.escola.jogador_tem_aptidao_magica_em", nome, resultado.textoColorido()), true);

            sucesso++;
        }

        return sucesso;
    }
}
