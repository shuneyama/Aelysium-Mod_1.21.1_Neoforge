package net.aelysium.aelysiummod.config;

public class ChatConfig {
    private static int localChatRadius = 40;
    private static boolean localChatEnabled = true;

    public static int getLocalChatRadius() {
        return localChatRadius;
    }

    public static void setLocalChatRadius(int radius) {
        localChatRadius = radius;
    }

    public static boolean isLocalChatEnabled() {
        return localChatEnabled;
    }

    public static void setLocalChatEnabled(boolean enabled) {
        localChatEnabled = enabled;
    }
}