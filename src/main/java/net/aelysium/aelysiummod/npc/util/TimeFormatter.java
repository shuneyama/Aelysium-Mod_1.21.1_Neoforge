package net.aelysium.aelysiummod.npc.util;

public class TimeFormatter {

    public static String formatTicks(long ticks) {
        long totalSeconds = ticks / 20;
        return formatSeconds(totalSeconds);
    }

    public static String formatSeconds(long totalSeconds) {
        if (totalSeconds <= 0) return "0s";

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("min ");
        if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append("s");
        return sb.toString().trim();
    }
}
