package net.aelysium.aelysiummod.time;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.world.scores.PlayerTeam;

public class TimeCorGerenciador {
    private static final Map<String, CustomTimeCor> teamColors = new HashMap<>();
    private static final Map<String, CustomTimeCor> prefixColors = new HashMap<>();
    private static final Map<String, CustomTimeCor> suffixColors = new HashMap<>();
    private static final Map<String, Boolean> hiddenTeams = new HashMap<>();

    // ==================== Team Colors ====================

    public static void setTeamColor(String teamName, CustomTimeCor color) {
        teamColors.put(teamName, color);
    }

    public static void setTeamColor(PlayerTeam team, CustomTimeCor color) {
        setTeamColor(team.getName(), color);
    }

    public static Optional<CustomTimeCor> getTeamColor(String teamName) {
        return Optional.ofNullable(teamColors.get(teamName));
    }

    public static Optional<CustomTimeCor> getTeamColor(PlayerTeam team) {
        return getTeamColor(team.getName());
    }

    public static void removeTeamColor(String teamName) {
        teamColors.remove(teamName);
    }

    public static boolean hasCustomColor(String teamName) {
        return teamColors.containsKey(teamName);
    }

    public static void clearAllColors() {
        teamColors.clear();
        prefixColors.clear();
        suffixColors.clear();
    }

    // ==================== Prefix Colors ====================

    public static void setPrefixColor(String teamName, CustomTimeCor color) {
        prefixColors.put(teamName, color);
    }

    public static Optional<CustomTimeCor> getPrefixColor(String teamName) {
        return Optional.ofNullable(prefixColors.get(teamName));
    }

    public static void removePrefixColor(String teamName) {
        prefixColors.remove(teamName);
    }

    public static boolean hasPrefixColor(String teamName) {
        return prefixColors.containsKey(teamName);
    }

    // ==================== Suffix Colors ====================

    public static void setSuffixColor(String teamName, CustomTimeCor color) {
        suffixColors.put(teamName, color);
    }

    public static Optional<CustomTimeCor> getSuffixColor(String teamName) {
        return Optional.ofNullable(suffixColors.get(teamName));
    }

    public static void removeSuffixColor(String teamName) {
        suffixColors.remove(teamName);
    }

    public static boolean hasSuffixColor(String teamName) {
        return suffixColors.containsKey(teamName);
    }

    // ==================== Formatting ====================

    public static void setTeamFormatting(String teamName, boolean bold, boolean italic,
                                         boolean underlined, boolean strikethrough, boolean obfuscated) {
        getTeamColor(teamName).ifPresent(color ->
                color.setBold(bold)
                        .setItalic(italic)
                        .setUnderlined(underlined)
                        .setStrikethrough(strikethrough)
                        .setObfuscated(obfuscated)
        );
    }

    public static void setPrefixFormatting(String teamName, boolean bold, boolean italic,
                                           boolean underlined, boolean strikethrough, boolean obfuscated) {
        getPrefixColor(teamName).ifPresent(color ->
                color.setBold(bold)
                        .setItalic(italic)
                        .setUnderlined(underlined)
                        .setStrikethrough(strikethrough)
                        .setObfuscated(obfuscated)
        );
    }

    public static void setSuffixFormatting(String teamName, boolean bold, boolean italic,
                                           boolean underlined, boolean strikethrough, boolean obfuscated) {
        getSuffixColor(teamName).ifPresent(color ->
                color.setBold(bold)
                        .setItalic(italic)
                        .setUnderlined(underlined)
                        .setStrikethrough(strikethrough)
                        .setObfuscated(obfuscated)
        );
    }

    // ==================== Hidden Teams (Jade) ====================

    public static void setTeamHidden(String teamName, boolean hidden) {
        if (hidden) {
            hiddenTeams.put(teamName, true);
        } else {
            hiddenTeams.remove(teamName);
        }
    }

    public static boolean isTeamHidden(String teamName) {
        return hiddenTeams.getOrDefault(teamName, false);
    }

    public static Set<String> getHiddenTeams() {
        return new HashSet<>(hiddenTeams.keySet());
    }
}