package net.aelysium.aelysiummod.time;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Set;
import net.aelysium.aelysiummod.jade.HiddenTeamSyncPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.network.PacketDistributor;

public class CorTimes {

    // ==================== Team Color (RGB/Hex) ====================

    public static int setRgbColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");
            int r = IntegerArgumentType.getInteger(context, "vermelho");
            int g = IntegerArgumentType.getInteger(context, "verde");
            int b = IntegerArgumentType.getInteger(context, "azul");

            CustomTimeCor color = new CustomTimeCor("custom", r, g, b);
            TimeCorGerenciador.setTeamColor(team, color);

            context.getSource().sendSuccess(() ->
                            Component.literal("Cor RGB do time ")
                                    .append(Component.literal(team.getName()).withColor(color.getRgb()))
                                    .append(" definida para ")
                                    .append(Component.literal(color.toHexString()).withColor(color.getRgb())),
                    true
            );

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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

            context.getSource().sendSuccess(() ->
                            Component.literal("Cor hexadecimal do time ")
                                    .append(Component.literal(team.getName()).withColor(color.getRgb()))
                                    .append(" definida para ")
                                    .append(Component.literal(color.toHexString()).withColor(color.getRgb())),
                    true
            );

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Erro ao processar hexadecimal. Use o formato #RRGGBB (exemplo: #FF69B4)")
            );
            return 0;
        }
    }

    public static int removeColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (TimeCorGerenciador.hasCustomColor(team.getName())) {
                TimeCorGerenciador.removeTeamColor(team.getName());
                context.getSource().sendSuccess(() ->
                                Component.literal("Cor customizada removida do time " + team.getName()),
                        true
                );
                TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
                return 1;
            }

            context.getSource().sendFailure(
                    Component.literal("O time " + team.getName() + " não possui cor customizada")
            );
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    // ==================== Prefix Color (RGB/Hex) ====================

    public static int setPrefixRgbColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");
            int r = IntegerArgumentType.getInteger(context, "vermelho");
            int g = IntegerArgumentType.getInteger(context, "verde");
            int b = IntegerArgumentType.getInteger(context, "azul");

            CustomTimeCor color = new CustomTimeCor("prefix", r, g, b);
            TimeCorGerenciador.setPrefixColor(team.getName(), color);

            context.getSource().sendSuccess(() ->
                            Component.literal("Cor RGB do prefixo do time ")
                                    .append(Component.literal(team.getName()).withColor(color.getRgb()))
                                    .append(" definida para ")
                                    .append(Component.literal(color.toHexString()).withColor(color.getRgb())),
                    true
            );

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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

            context.getSource().sendSuccess(() ->
                            Component.literal("Cor hexadecimal do prefixo do time ")
                                    .append(Component.literal(team.getName()).withColor(color.getRgb()))
                                    .append(" definida para ")
                                    .append(Component.literal(color.toHexString()).withColor(color.getRgb())),
                    true
            );

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Erro ao processar hexadecimal. Use o formato #RRGGBB (exemplo: #FF69B4)")
            );
            return 0;
        }
    }

    public static int removePrefixColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (TimeCorGerenciador.hasPrefixColor(team.getName())) {
                TimeCorGerenciador.removePrefixColor(team.getName());
                context.getSource().sendSuccess(() ->
                                Component.literal("Cor do prefixo removida do time " + team.getName()),
                        true
                );
                TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
                return 1;
            }

            context.getSource().sendFailure(
                    Component.literal("O time " + team.getName() + " não possui cor de prefixo customizada")
            );
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    // ==================== Suffix Color (RGB/Hex) ====================

    public static int setSuffixRgbColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");
            int r = IntegerArgumentType.getInteger(context, "vermelho");
            int g = IntegerArgumentType.getInteger(context, "verde");
            int b = IntegerArgumentType.getInteger(context, "azul");

            CustomTimeCor color = new CustomTimeCor("suffix", r, g, b);
            TimeCorGerenciador.setSuffixColor(team.getName(), color);

            context.getSource().sendSuccess(() ->
                            Component.literal("Cor RGB do sufixo do time ")
                                    .append(Component.literal(team.getName()).withColor(color.getRgb()))
                                    .append(" definida para ")
                                    .append(Component.literal(color.toHexString()).withColor(color.getRgb())),
                    true
            );

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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

            context.getSource().sendSuccess(() ->
                            Component.literal("Cor hexadecimal do sufixo do time ")
                                    .append(Component.literal(team.getName()).withColor(color.getRgb()))
                                    .append(" definida para ")
                                    .append(Component.literal(color.toHexString()).withColor(color.getRgb())),
                    true
            );

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Erro ao processar hexadecimal. Use o formato #RRGGBB (exemplo: #FF69B4)")
            );
            return 0;
        }
    }

    public static int removeSuffixColor(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (TimeCorGerenciador.hasSuffixColor(team.getName())) {
                TimeCorGerenciador.removeSuffixColor(team.getName());
                context.getSource().sendSuccess(() ->
                                Component.literal("Cor do sufixo removida do time " + team.getName()),
                        true
                );
                TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
                return 1;
            }

            context.getSource().sendFailure(
                    Component.literal("O time " + team.getName() + " não possui cor de sufixo customizada")
            );
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    // ==================== Name Formatting Toggles ====================

    public static int toggleNameBold(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasCustomColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setTeamColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getTeamColor(team.getName()).ifPresent(color -> {
                color.setBold(!color.isBold());
                String status = color.isBold() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Negrito do nome do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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
                String status = color.isItalic() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Itálico do nome do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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
                String status = color.isUnderlined() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Sublinhado do nome do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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
                String status = color.isStrikethrough() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Riscado do nome do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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
                String status = color.isObfuscated() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Ofuscado do nome do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    // ==================== Prefix Formatting Toggles ====================

    public static int togglePrefixBold(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasPrefixColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setPrefixColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getPrefixColor(team.getName()).ifPresent(color -> {
                color.setBold(!color.isBold());
                String status = color.isBold() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Negrito do prefixo do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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
                String status = color.isItalic() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Itálico do prefixo do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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
                String status = color.isUnderlined() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Sublinhado do prefixo do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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
                String status = color.isStrikethrough() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Riscado do prefixo do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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
                String status = color.isObfuscated() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Ofuscado do prefixo do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    // ==================== Suffix Formatting Toggles ====================

    public static int toggleSuffixBold(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");

            if (!TimeCorGerenciador.hasSuffixColor(team.getName())) {
                CustomTimeCor defaultColor = new CustomTimeCor("default", 255, 255, 255);
                TimeCorGerenciador.setSuffixColor(team.getName(), defaultColor);
            }

            TimeCorGerenciador.getSuffixColor(team.getName()).ifPresent(color -> {
                color.setBold(!color.isBold());
                String status = color.isBold() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Negrito do sufixo do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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
                String status = color.isItalic() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Itálico do sufixo do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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
                String status = color.isUnderlined() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Sublinhado do sufixo do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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
                String status = color.isStrikethrough() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Riscado do sufixo do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
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
                String status = color.isObfuscated() ? "ativado" : "desativado";
                context.getSource().sendSuccess(() ->
                                Component.literal("Ofuscado do sufixo do time " + team.getName() + " " + status),
                        true
                );
            });

            TabLista.updateAllPlayersTabList(context.getSource().getPlayer());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    // ==================== Jade Integration ====================

    public static int hideTeamInJade(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");
            TimeCorGerenciador.setTeamHidden(team.getName(), true);

            HiddenTeamSyncPacket packet = new HiddenTeamSyncPacket(TimeCorGerenciador.getHiddenTeams());
            PacketDistributor.sendToAllPlayers(packet);

            context.getSource().sendSuccess(() ->
                            Component.literal("Time " + team.getName() + " agora terá nomes ocultos no Jade"),
                    true
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int showTeamInJade(CommandContext<CommandSourceStack> context) {
        try {
            PlayerTeam team = TeamArgument.getTeam(context, "time");
            TimeCorGerenciador.setTeamHidden(team.getName(), false);

            HiddenTeamSyncPacket packet = new HiddenTeamSyncPacket(TimeCorGerenciador.getHiddenTeams());
            PacketDistributor.sendToAllPlayers(packet);

            context.getSource().sendSuccess(() ->
                            Component.literal("Time " + team.getName() + " agora terá nomes visíveis no Jade"),
                    true
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Erro: " + e.getMessage()));
            return 0;
        }
    }

    public static int listHiddenTeams(CommandContext<CommandSourceStack> context) {
        Set<String> hiddenTeams = TimeCorGerenciador.getHiddenTeams();

        if (hiddenTeams.isEmpty()) {
            context.getSource().sendSuccess(() ->
                            Component.literal("Nenhum time está oculto no Jade"),
                    false
            );
        } else {
            context.getSource().sendSuccess(() ->
                            Component.literal("Times ocultos no Jade:"),
                    false
            );
            hiddenTeams.forEach(teamName ->
                    context.getSource().sendSuccess(() ->
                                    Component.literal("  - " + teamName),
                            false
                    )
            );
        }
        return 1;
    }
}