package net.aelysium.aelysiummod.sistemas.mundo.dificuldade.comando;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.sistemas.mundo.dificuldade.ConfiguracaoDificuldade;
import net.aelysium.aelysiummod.sistemas.mundo.dificuldade.DadosDificuldade;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ComandoDificuldade {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("dificuldade")
                .requires(origem -> origem.hasPermission(2))
                .then(Commands.literal("verificar").executes(ComandoDificuldade::mostrar))
                .then(Commands.literal("recarregar").executes(ComandoDificuldade::recarregar))
                .then(Commands.literal("definir")
                        .then(Commands.argument("nivel", IntegerArgumentType.integer(0, DadosDificuldade.NIVEL_MAXIMO))
                                .executes(ctx -> definir(ctx, null, null))
                                .then(Commands.argument("griefing", BoolArgumentType.bool())
                                        .executes(ctx -> definir(ctx, BoolArgumentType.getBool(ctx, "griefing"), null))
                                        .then(Commands.argument("hordas", BoolArgumentType.bool())
                                                .executes(ctx -> definir(ctx, BoolArgumentType.getBool(ctx, "griefing"), BoolArgumentType.getBool(ctx, "hordas")))))));
    }

    private static int mostrar(CommandContext<CommandSourceStack> ctx) {
        DadosDificuldade dados = DadosDificuldade.get(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(() -> resumo(dados), false);
        return dados.getNivel();
    }

    private static int recarregar(CommandContext<CommandSourceStack> ctx) {
        ConfiguracaoDificuldade.carregar(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(() -> Component.translatable("mensagem.aelysiummod.dificuldade.configuracao_recarregada"), true);
        return 1;
    }

    private static int definir(CommandContext<CommandSourceStack> ctx, Boolean griefing, Boolean hordas) {
        DadosDificuldade dados = DadosDificuldade.get(ctx.getSource().getServer());
        int nivel = IntegerArgumentType.getInteger(ctx, "nivel");
        dados.definir(nivel, griefing != null ? griefing : dados.isGriefing(), hordas != null ? hordas : dados.isHordas());

        if (nivel == 0) {
            ctx.getSource().sendSuccess(() -> Component.translatable("mensagem.aelysiummod.dificuldade.desligada"), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable("mensagem.aelysiummod.dificuldade.definida").append(" ").append(resumo(dados)), true);
        }
        return nivel;
    }

    private static Component resumo(DadosDificuldade dados) {
        return Component.translatableEscape("mensagem.aelysiummod.dificuldade.resumo",
                dados.getNivel(), estado(dados.isGriefing()), estado(dados.isHordas()));
    }

    private static Component estado(boolean ativo) {
        return Component.translatable(ativo ? "mensagem.aelysiummod.comum.ativado" : "mensagem.aelysiummod.comum.desativado");
    }
}
