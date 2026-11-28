package net.aelysium.aelysiummod.nucleo.comando.condicao;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.aelysium.aelysiummod.sistemas.mundo.dificuldade.DadosDificuldade;
import net.aelysium.aelysiummod.sistemas.mundo.lua.GerenciadorLua;
import net.aelysium.aelysiummod.sistemas.mundo.lua.TipoLua;
import net.aelysium.aelysiummod.sistemas.rp.habilidade.GerenciadorHabilidade;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.RangeArgument;

public final class CondicoesMundo {

    private CondicoesMundo() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> lua(CondicoesExecute.Ramo ramo) {
        return Commands.literal("lua")
                .then(ramo.condicao(Commands.literal("vermelha"), ctx -> GerenciadorLua.luaAtual == TipoLua.VERMELHA))
                .then(ramo.condicao(Commands.literal("normal"), ctx -> GerenciadorLua.luaAtual == TipoLua.NORMAL));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> manutencao(CondicoesExecute.Ramo ramo) {
        return ramo.condicao(Commands.literal("manutencao"), ctx -> GerenciadorHabilidade.isManutencaoAtiva());
    }

    public static LiteralArgumentBuilder<CommandSourceStack> dificuldade(CondicoesExecute.Ramo ramo) {
        return Commands.literal("dificuldade")
                .then(Commands.literal("nivel").then(ramo.condicao(Commands.argument("faixa", RangeArgument.intRange()),
                        ctx -> RangeArgument.Ints.getRange(ctx, "faixa").matches(DadosDificuldade.get(ctx.getSource().getServer()).getNivel()))))
                .then(ramo.condicao(Commands.literal("griefing"), ctx -> DadosDificuldade.get(ctx.getSource().getServer()).isGriefing()))
                .then(ramo.condicao(Commands.literal("hordas"), ctx -> DadosDificuldade.get(ctx.getSource().getServer()).isHordas()));
    }
}
