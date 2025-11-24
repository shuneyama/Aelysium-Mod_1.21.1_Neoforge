package net.aelysium.aelysiummod.command.chat;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.team.CustomTimeCor;
import net.aelysium.aelysiummod.team.TimeCorGerenciador;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;

public class CorTimes {

    public static int setRgbColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "team");
            int r = IntegerArgumentType.getInteger(context, "red");
            int g = IntegerArgumentType.getInteger(context, "green");
            int b = IntegerArgumentType.getInteger(context, "blue");

            CustomTimeCor color = new CustomTimeCor("custom", r, g, b);

            TimeCorGerenciador.setTeamColor(team, color);

            context.getSource().sendSuccess(
                    () -> Component.literal("Cor RGB do time ")
                            .append(Component.literal(team.getName()).withColor(color.getRgb()))
                            .append(" definida para ")
                            .append(Component.literal(color.toHexString()).withColor(color.getRgb())),
                    true
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int setHexColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "team");
            String hex = StringArgumentType.getString(context, "hexcode");

            CustomTimeCor color = CustomTimeCor.fromHex("custom", hex);

            TimeCorGerenciador.setTeamColor(team, color);

            context.getSource().sendSuccess(
                    () -> Component.literal("Cor hexadecimal do time ")
                            .append(Component.literal(team.getName()).withColor(color.getRgb()))
                            .append(" definida para ")
                            .append(Component.literal(color.toHexString()).withColor(color.getRgb())),
                    true
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Erro ao processar código hexadecimal. Use o formato #RRGGBB (exemplo: #FF69B4)")
            );
            return 0;
        }
    }

    public static int removeColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "team");

            if (TimeCorGerenciador.hasCustomColor(team.getName())) {
                TimeCorGerenciador.removeTeamColor(team.getName());

                context.getSource().sendSuccess(
                        () -> Component.literal("Cor customizada removida do time " + team.getName()),
                        true
                );
                return 1;
            } else {
                context.getSource().sendFailure(
                        Component.literal("O time " + team.getName() + " não possui cor customizada")
                );
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }
}