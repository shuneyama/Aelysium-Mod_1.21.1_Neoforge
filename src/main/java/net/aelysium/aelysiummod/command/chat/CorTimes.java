package net.aelysium.aelysiummod.command.chat;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.team.CustomTimeCor;
import net.aelysium.aelysiummod.team.TabListHandler;
import net.aelysium.aelysiummod.team.TimeCorGerenciador;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;

public class CorTimes {

    public static int setRgbColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");
            int r = IntegerArgumentType.getInteger(context, "vermelho");
            int g = IntegerArgumentType.getInteger(context, "verde");
            int b = IntegerArgumentType.getInteger(context, "azul");

            CustomTimeCor color = new CustomTimeCor("custom", r, g, b);
            TimeCorGerenciador.setTeamColor(team, color);

            context.getSource().sendSuccess(
                    () -> Component.literal("Cor RGB do time ")
                            .append(Component.literal(team.getName()).withColor(color.getRgb()))
                            .append(" definida para ")
                            .append(Component.literal(color.toHexString()).withColor(color.getRgb())),
                    true
            );

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int setHexColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");
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

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
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
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (TimeCorGerenciador.hasCustomColor(team.getName())) {
                TimeCorGerenciador.removeTeamColor(team.getName());

                context.getSource().sendSuccess(
                        () -> Component.literal("Cor customizada removida do time " + team.getName()),
                        true
                );

                TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
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

// ===== CORES DO PREFIXO =====

    public static int setPrefixRgbColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");
            int r = IntegerArgumentType.getInteger(context, "vermelho");
            int g = IntegerArgumentType.getInteger(context, "verde");
            int b = IntegerArgumentType.getInteger(context, "azul");

            CustomTimeCor color = new CustomTimeCor("prefix", r, g, b);
            TimeCorGerenciador.setPrefixColor(team.getName(), color);

            context.getSource().sendSuccess(
                    () -> Component.literal("Cor RGB do prefixo do time ")
                            .append(Component.literal(team.getName()).withColor(color.getRgb()))
                            .append(" definida para ")
                            .append(Component.literal(color.toHexString()).withColor(color.getRgb())),
                    true
            );

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int setPrefixHexColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");
            String hex = StringArgumentType.getString(context, "hexcode");

            CustomTimeCor color = CustomTimeCor.fromHex("prefix", hex);
            TimeCorGerenciador.setPrefixColor(team.getName(), color);

            context.getSource().sendSuccess(
                    () -> Component.literal("Cor hexadecimal do prefixo do time ")
                            .append(Component.literal(team.getName()).withColor(color.getRgb()))
                            .append(" definida para ")
                            .append(Component.literal(color.toHexString()).withColor(color.getRgb())),
                    true
            );

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Erro ao processar código hexadecimal. Use o formato #RRGGBB (exemplo: #FF69B4)")
            );
            return 0;
        }
    }

    public static int removePrefixColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (TimeCorGerenciador.hasPrefixColor(team.getName())) {
                TimeCorGerenciador.removePrefixColor(team.getName());

                context.getSource().sendSuccess(
                        () -> Component.literal("Cor do prefixo removida do time " + team.getName()),
                        true
                );

                TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
                return 1;
            } else {
                context.getSource().sendFailure(
                        Component.literal("O time " + team.getName() + " não possui cor de prefixo customizada")
                );
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

// ===== CORES DO SUFIXO =====

    public static int setSuffixRgbColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");
            int r = IntegerArgumentType.getInteger(context, "vermelho");
            int g = IntegerArgumentType.getInteger(context, "verde");
            int b = IntegerArgumentType.getInteger(context, "azul");

            CustomTimeCor color = new CustomTimeCor("suffix", r, g, b);
            TimeCorGerenciador.setSuffixColor(team.getName(), color);

            context.getSource().sendSuccess(
                    () -> Component.literal("Cor RGB do sufixo do time ")
                            .append(Component.literal(team.getName()).withColor(color.getRgb()))
                            .append(" definida para ")
                            .append(Component.literal(color.toHexString()).withColor(color.getRgb())),
                    true
            );

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int setSuffixHexColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");
            String hex = StringArgumentType.getString(context, "hexcode");

            CustomTimeCor color = CustomTimeCor.fromHex("suffix", hex);
            TimeCorGerenciador.setSuffixColor(team.getName(), color);

            context.getSource().sendSuccess(
                    () -> Component.literal("Cor hexadecimal do sufixo do time ")
                            .append(Component.literal(team.getName()).withColor(color.getRgb()))
                            .append(" definida para ")
                            .append(Component.literal(color.toHexString()).withColor(color.getRgb())),
                    true
            );

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Erro ao processar código hexadecimal. Use o formato #RRGGBB (exemplo: #FF69B4)")
            );
            return 0;
        }
    }

    public static int removeSuffixColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (TimeCorGerenciador.hasSuffixColor(team.getName())) {
                TimeCorGerenciador.removeSuffixColor(team.getName());

                context.getSource().sendSuccess(
                        () -> Component.literal("Cor do sufixo removida do time " + team.getName()),
                        true
                );

                TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
                return 1;
            } else {
                context.getSource().sendFailure(
                        Component.literal("O time " + team.getName() + " não possui cor de sufixo customizada")
                );
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    // ===== FORMATAÇÃO DO NOME =====

    public static int toggleNameBold(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            // Se não tem cor, cria uma cor "padrão" (branca)
            if (!TimeCorGerenciador.hasCustomColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setTeamColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getTeamColor(team.getName()).ifPresent(color -> {
                color.setBold(!color.isBold());
                context.getSource().sendSuccess(
                        () -> Component.literal("Negrito do nome: " + (color.isBold() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int toggleNameItalic(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasCustomColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setTeamColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getTeamColor(team.getName()).ifPresent(color -> {
                color.setItalic(!color.isItalic());
                context.getSource().sendSuccess(
                        () -> Component.literal("Itálico do nome: " + (color.isItalic() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int toggleNameUnderlined(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasCustomColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setTeamColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getTeamColor(team.getName()).ifPresent(color -> {
                color.setUnderlined(!color.isUnderlined());
                context.getSource().sendSuccess(
                        () -> Component.literal("Sublinhado do nome: " + (color.isUnderlined() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int toggleNameStrikethrough(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasCustomColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setTeamColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getTeamColor(team.getName()).ifPresent(color -> {
                color.setStrikethrough(!color.isStrikethrough());
                context.getSource().sendSuccess(
                        () -> Component.literal("Riscado do nome: " + (color.isStrikethrough() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int toggleNameObfuscated(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasCustomColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setTeamColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getTeamColor(team.getName()).ifPresent(color -> {
                color.setObfuscated(!color.isObfuscated());
                context.getSource().sendSuccess(
                        () -> Component.literal("Ofuscado/Zalgo do nome: " + (color.isObfuscated() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

// ===== FORMATAÇÃO DO PREFIXO =====

    public static int togglePrefixBold(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasPrefixColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setPrefixColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getPrefixColor(team.getName()).ifPresent(color -> {
                color.setBold(!color.isBold());
                context.getSource().sendSuccess(
                        () -> Component.literal("Negrito do prefixo: " + (color.isBold() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int togglePrefixItalic(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasPrefixColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setPrefixColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getPrefixColor(team.getName()).ifPresent(color -> {
                color.setItalic(!color.isItalic());
                context.getSource().sendSuccess(
                        () -> Component.literal("Itálico do prefixo: " + (color.isItalic() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int togglePrefixUnderlined(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasPrefixColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setPrefixColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getPrefixColor(team.getName()).ifPresent(color -> {
                color.setUnderlined(!color.isUnderlined());
                context.getSource().sendSuccess(
                        () -> Component.literal("Sublinhado do prefixo: " + (color.isUnderlined() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int togglePrefixStrikethrough(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasPrefixColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setPrefixColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getPrefixColor(team.getName()).ifPresent(color -> {
                color.setStrikethrough(!color.isStrikethrough());
                context.getSource().sendSuccess(
                        () -> Component.literal("Riscado do prefixo: " + (color.isStrikethrough() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int togglePrefixObfuscated(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasPrefixColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setPrefixColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getPrefixColor(team.getName()).ifPresent(color -> {
                color.setObfuscated(!color.isObfuscated());
                context.getSource().sendSuccess(
                        () -> Component.literal("Ofuscado/Zalgo do prefixo: " + (color.isObfuscated() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

// ===== FORMATAÇÃO DO SUFIXO =====

    public static int toggleSuffixBold(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasSuffixColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setSuffixColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getSuffixColor(team.getName()).ifPresent(color -> {
                color.setBold(!color.isBold());
                context.getSource().sendSuccess(
                        () -> Component.literal("Negrito do sufixo: " + (color.isBold() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int toggleSuffixItalic(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasSuffixColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setSuffixColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getSuffixColor(team.getName()).ifPresent(color -> {
                color.setItalic(!color.isItalic());
                context.getSource().sendSuccess(
                        () -> Component.literal("Itálico do sufixo: " + (color.isItalic() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int toggleSuffixUnderlined(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasSuffixColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setSuffixColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getSuffixColor(team.getName()).ifPresent(color -> {
                color.setUnderlined(!color.isUnderlined());
                context.getSource().sendSuccess(
                        () -> Component.literal("Sublinhado do sufixo: " + (color.isUnderlined() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int toggleSuffixStrikethrough(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasSuffixColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setSuffixColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getSuffixColor(team.getName()).ifPresent(color -> {
                color.setStrikethrough(!color.isStrikethrough());
                context.getSource().sendSuccess(
                        () -> Component.literal("Riscado do sufixo: " + (color.isStrikethrough() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int toggleSuffixObfuscated(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasSuffixColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setSuffixColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getSuffixColor(team.getName()).ifPresent(color -> {
                color.setObfuscated(!color.isObfuscated());
                context.getSource().sendSuccess(
                        () -> Component.literal("Ofuscado/Zalgo do sufixo: " + (color.isObfuscated() ? "§aAtivado" : "§cDesativado")),
                        true
                );
            });

            TabListHandler.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }
}