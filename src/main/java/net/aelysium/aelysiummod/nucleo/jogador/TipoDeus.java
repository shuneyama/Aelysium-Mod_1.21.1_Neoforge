package net.aelysium.aelysiummod.nucleo.jogador;

public enum TipoDeus {

    NONE("none", "Nenhum", 0x000000),
    KAIROS("kairos", "Kairos", 0xFFD700),
    RONOVA("ronova", "Ronova", 0x1A1A1A),
    AZARUS("azarus", "Azarus", 0x006994),
    YANCY("yancy", "Yancy", 0x87CEEB),
    ASHYRA("ashyra", "Ashyra", 0xFF8C00),
    EREBOS("erebos", "Erebos", 0x8B008B),
    PHANES("phanes", "Phanes", 0xF0F0F0),
    KLAUS("klaus", "Klaus", 0x00CED1),
    FREYR("freyr", "Freyr", 0x32CD32);

    public final String id;
    public final String displayName;
    public final int color;

    TipoDeus(String id, String displayName, int color) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
    }

    public static TipoDeus fromId(String id) {
        for (TipoDeus d : values()) {
            if (d.id.equalsIgnoreCase(id)) return d;
        }
        return NONE;
    }
}