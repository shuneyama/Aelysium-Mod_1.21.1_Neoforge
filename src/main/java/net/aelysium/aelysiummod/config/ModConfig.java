package net.aelysium.aelysiummod.config;

public class ModConfig {
    private static boolean joinLeaveMessagesEnabled = true;

    public static void setJoinLeaveMessagesEnabled(boolean enabled) {
        joinLeaveMessagesEnabled = enabled;
    }

    public static boolean areJoinLeaveMessagesEnabled() {
        return joinLeaveMessagesEnabled;
    }

    public static boolean toggleJoinLeaveMessages() {
        joinLeaveMessagesEnabled = !joinLeaveMessagesEnabled;
        return joinLeaveMessagesEnabled;
    }
}