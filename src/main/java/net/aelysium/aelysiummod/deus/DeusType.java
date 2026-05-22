package net.aelysium.aelysiummod.deus;

public enum DeusType {

    NONE("none", "Nenhum", 0x000000),
    KAIROS("kairos", "Kairos", 0xFFD700),
    RONOVA("ronova", "Ronova", 0x1A1A1A),
    AZARUS("azarus", "Azarus", 0x006994),
    DAMSELETTE("damselette", "Damselette", 0x87CEEB),
    ASHYRA("ashyra", "Ashyra", 0xFF8C00),
    VELGRYND("velgrynd", "Velgrynd", 0x8B008B),
    VELZARD("velzard", "Velzard", 0xF0F0F0),
    KLAUS("klaus", "Klaus", 0x00CED1);

    public final String id;
    public final String displayName;
    public final int color;

    DeusType(String id, String displayName, int color) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
    }

    public static DeusType fromId(String id) {
        for (DeusType d : values()) {
            if (d.id.equalsIgnoreCase(id)) return d;
        }
        return NONE;
    }
}