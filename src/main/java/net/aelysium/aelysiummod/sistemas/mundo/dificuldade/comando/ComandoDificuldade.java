package net.aelysium.aelysiummod.sistemas.mundo.dificuldade.comando;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.nucleo.comando.TextoComando;
import net.aelysium.aelysiummod.sistemas.mundo.dificuldade.ConfiguracaoDificuldade;
import net.aelysium.aelysiummod.sistemas.mundo.dificuldade.DadosDificuldade;
import net.aelysium.aelysiummod.sistemas.mundo.dificuldade.PerfilDificuldade;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ComandoDificuldade {
    private static final String P = "mensagem.aelysiummod.dificuldade.";
    private static final DecimalFormat FORMATO = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ROOT));

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("dificuldade")
                .requires(origem -> origem.hasPermission(2))
                .then(Commands.literal("status").executes(ComandoDificuldade::status))
                .then(Commands.literal("nivel")
                        .then(Commands.argument("nivel", IntegerArgumentType.integer(0, DadosDificuldade.NIVEL_MAXIMO))
                                .executes(ComandoDificuldade::nivel)))
                .then(Commands.literal("griefing")
                        .then(Commands.literal("ligar").executes(ctx -> griefing(ctx, true)))
                        .then(Commands.literal("desligar").executes(ctx -> griefing(ctx, false))))
                .then(Commands.literal("hordas")
                        .then(Commands.literal("ligar").executes(ctx -> hordas(ctx, true)))
                        .then(Commands.literal("desligar").executes(ctx -> hordas(ctx, false))))
                .then(Commands.literal("recarregar").executes(ComandoDificuldade::recarregar));
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack fonte = ctx.getSource();
        DadosDificuldade dados = DadosDificuldade.get(fonte.getServer());
        int nivel = dados.getNivel();
        fonte.sendSuccess(() -> Component.translatable(P + "status_titulo"), false);
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "status_nivel", ChatFormatting.GRAY, nivel, DadosDificuldade.NIVEL_MAXIMO), false);
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "status_griefing", ChatFormatting.GRAY, estado(dados.isGriefing())), false);
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "status_hordas", ChatFormatting.GRAY, estado(dados.isHordas())), false);
        if (nivel <= 0) {
            fonte.sendSuccess(() -> Component.translatable(P + "status_desligada"), false);
            return 0;
        }
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "efeito_vida", ChatFormatting.GRAY, "+" + porcento(PerfilDificuldade.bonusVida(nivel))), false);
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "efeito_dano", ChatFormatting.GRAY, "+" + porcento(PerfilDificuldade.bonusDano(nivel))), false);
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "efeito_velocidade", ChatFormatting.GRAY, "+" + porcento(PerfilDificuldade.bonusVelocidade(nivel))), false);
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "efeito_armadura", ChatFormatting.GRAY, "+" + FORMATO.format(PerfilDificuldade.bonusArmadura(nivel))), false);
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "efeito_empurrao", ChatFormatting.GRAY, "+" + porcento(PerfilDificuldade.bonusResistenciaEmpurrao(nivel))), false);
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "efeito_penetracao", ChatFormatting.GRAY, porcento(PerfilDificuldade.penetracaoArmadura(nivel))), false);
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "efeito_golpe", ChatFormatting.GRAY,
                porcento(PerfilDificuldade.chanceGolpeEspecial(nivel)),
                FORMATO.format(PerfilDificuldade.recargaGolpeEspecialTicks(nivel) / 20.0)), false);
        return nivel;
    }

    private static int nivel(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack fonte = ctx.getSource();
        DadosDificuldade dados = DadosDificuldade.get(fonte.getServer());
        int nivel = IntegerArgumentType.getInteger(ctx, "nivel");
        dados.definir(nivel, dados.isGriefing(), dados.isHordas());
        if (nivel == 0) {
            fonte.sendSuccess(() -> Component.translatable(P + "desligada"), true);
        } else {
            fonte.sendSuccess(() -> TextoComando.traduzir(P + "nivel_definido", ChatFormatting.GREEN, nivel), true);
            fonte.sendSuccess(() -> Component.translatable(P + "aviso_novos"), false);
        }
        return nivel;
    }

    private static int griefing(CommandContext<CommandSourceStack> ctx, boolean ligar) {
        DadosDificuldade dados = DadosDificuldade.get(ctx.getSource().getServer());
        dados.definir(dados.getNivel(), ligar, dados.isHordas());
        ctx.getSource().sendSuccess(() -> Component.translatable(P + (ligar ? "griefing_ligado" : "griefing_desligado")), true);
        return 1;
    }

    private static int hordas(CommandContext<CommandSourceStack> ctx, boolean ligar) {
        DadosDificuldade dados = DadosDificuldade.get(ctx.getSource().getServer());
        dados.definir(dados.getNivel(), dados.isGriefing(), ligar);
        ctx.getSource().sendSuccess(() -> Component.translatable(P + (ligar ? "hordas_ligadas" : "hordas_desligadas")), true);
        return 1;
    }

    private static int recarregar(CommandContext<CommandSourceStack> ctx) {
        ConfiguracaoDificuldade.carregar(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(() -> Component.translatable(P + "configuracao_recarregada"), true);
        return 1;
    }

    private static Component estado(boolean ativo) {
        return Component.translatable(ativo ? "mensagem.aelysiummod.comum.ativado" : "mensagem.aelysiummod.comum.desativado");
    }

    private static String porcento(double valor) {
        return FORMATO.format(valor * 100.0) + "%";
    }
}
