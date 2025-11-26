package net.aelysium.aelysiummod.time;

import net.minecraft.world.scores.PlayerTeam;

import java.util.*;

/**
 * Gerencia as cores customizadas atribuídas aos times
 */
public class TimeCorGerenciador {
    private static final Map<String, CustomTimeCor> teamColors = new HashMap<>();
    private static final Map<String, CustomTimeCor> prefixColors = new HashMap<>();
    private static final Map<String, CustomTimeCor> suffixColors = new HashMap<>();
    private static final Map<String, Boolean> hiddenTeams = new HashMap<>();

    /**
     * Define uma cor customizada para um time
     */
    public static void setTeamColor(String teamName, CustomTimeCor color) {
        teamColors.put(teamName, color);
    }

    /**
     * Define uma cor customizada para um time usando PlayerTeam
     */
    public static void setTeamColor(PlayerTeam team, CustomTimeCor color) {
        setTeamColor(team.getName(), color);
    }

    /**
     * Obtém a cor customizada de um time
     */
    public static Optional<CustomTimeCor> getTeamColor(String teamName) {
        return Optional.ofNullable(teamColors.get(teamName));
    }

    /**
     * Obtém a cor customizada de um time usando PlayerTeam
     */
    public static Optional<CustomTimeCor> getTeamColor(PlayerTeam team) {
        return getTeamColor(team.getName());
    }

    /**
     * Remove a cor customizada de um time
     */
    public static void removeTeamColor(String teamName) {
        teamColors.remove(teamName);
    }

    /**
     * Verifica se um time tem cor customizada
     */
    public static boolean hasCustomColor(String teamName) {
        return teamColors.containsKey(teamName);
    }

    /**
     * Limpa todas as cores customizadas
     */
    public static void clearAllColors() {
        teamColors.clear();
        prefixColors.clear();
        suffixColors.clear();
    }

    // ===== MÉTODOS PARA PREFIXO =====

    /**
     * Define uma cor customizada para o prefixo de um time
     */
    public static void setPrefixColor(String teamName, CustomTimeCor color) {
        prefixColors.put(teamName, color);
    }

    /**
     * Obtém a cor customizada do prefixo de um time
     */
    public static Optional<CustomTimeCor> getPrefixColor(String teamName) {
        return Optional.ofNullable(prefixColors.get(teamName));
    }

    /**
     * Remove a cor customizada do prefixo de um time
     */
    public static void removePrefixColor(String teamName) {
        prefixColors.remove(teamName);
    }

    /**
     * Verifica se um time tem cor de prefixo customizada
     */
    public static boolean hasPrefixColor(String teamName) {
        return prefixColors.containsKey(teamName);
    }

    // ===== MÉTODOS PARA SUFIXO =====

    /**
     * Define uma cor customizada para o sufixo de um time
     */
    public static void setSuffixColor(String teamName, CustomTimeCor color) {
        suffixColors.put(teamName, color);
    }

    /**
     * Obtém a cor customizada do sufixo de um time
     */
    public static Optional<CustomTimeCor> getSuffixColor(String teamName) {
        return Optional.ofNullable(suffixColors.get(teamName));
    }

    /**
     * Remove a cor customizada do sufixo de um time
     */
    public static void removeSuffixColor(String teamName) {
        suffixColors.remove(teamName);
    }

    /**
     * Verifica se um time tem cor de sufixo customizada
     */
    public static boolean hasSuffixColor(String teamName) {
        return suffixColors.containsKey(teamName);
    }

    // ===== MÉTODOS PARA APLICAR FORMATAÇÕES =====

    /**
     * Aplica formatação ao nome do time
     */
    public static void setTeamFormatting(String teamName, boolean bold, boolean italic,
                                         boolean underlined, boolean strikethrough, boolean obfuscated) {
        getTeamColor(teamName).ifPresent(color -> {
            color.setBold(bold)
                    .setItalic(italic)
                    .setUnderlined(underlined)
                    .setStrikethrough(strikethrough)
                    .setObfuscated(obfuscated);
        });
    }

    /**
     * Aplica formatação ao prefixo
     */
    public static void setPrefixFormatting(String teamName, boolean bold, boolean italic,
                                           boolean underlined, boolean strikethrough, boolean obfuscated) {
        getPrefixColor(teamName).ifPresent(color -> {
            color.setBold(bold)
                    .setItalic(italic)
                    .setUnderlined(underlined)
                    .setStrikethrough(strikethrough)
                    .setObfuscated(obfuscated);
        });
    }

    /**
     * Aplica formatação ao sufixo
     */
    public static void setSuffixFormatting(String teamName, boolean bold, boolean italic,
                                           boolean underlined, boolean strikethrough, boolean obfuscated) {
        getSuffixColor(teamName).ifPresent(color -> {
            color.setBold(bold)
                    .setItalic(italic)
                    .setUnderlined(underlined)
                    .setStrikethrough(strikethrough)
                    .setObfuscated(obfuscated);
        });
    }

    /**
     * Define se um time deve ter nomes ocultos no Jade
     */
    public static void setTeamHidden(String teamName, boolean hidden) {
        if (hidden) {
            hiddenTeams.put(teamName, true);
        } else {
            hiddenTeams.remove(teamName);
        }
    }

    /**
     * Verifica se um time está marcado como oculto
     */
    public static boolean isTeamHidden(String teamName) {
        return hiddenTeams.getOrDefault(teamName, false);
    }

    /**
     * Obtém todos os times ocultos
     */
    public static Set<String> getHiddenTeams() {
        return new HashSet<>(hiddenTeams.keySet());
    }
}