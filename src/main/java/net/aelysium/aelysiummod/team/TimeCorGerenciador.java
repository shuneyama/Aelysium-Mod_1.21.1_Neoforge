package net.aelysium.aelysiummod.team;

import net.minecraft.world.scores.PlayerTeam;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TimeCorGerenciador {
    private static final Map<String, CustomTimeCor> teamColors = new HashMap<>();

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
    }
}