package net.aelysium.aelysiummod.nucleo.jogador;

import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public enum TipoInsignia {

    ADMIN("admin", 0xE000, 30, 10),
    DEUS("deus", 0xE001, 27, 10),
    CIENTISTA("cientista", 0xE00A, 44, 10),
    ROBO2("robo2", 0xE009, 27, 10),
    DRACONO("dracono", 0xE002, 42, 10),
    ELVARIN("elvarin", 0xE003, 37, 10),
    HUMANO("humano", 0xE004, 38, 10),
    TIEFLING("tiefling", 0xE005, 40, 10),
    UNDYNE("undyne", 0xE006, 36, 10),
    VALKYRIA("valkyria", 0xE007, 41, 10);


    public final String id;
    public final int glyphCodePoint;
    public final int textureWidth;
    public final int textureHeight;

    TipoInsignia(String id, int glyphCodePoint, int textureWidth, int textureHeight) {
        this.id = id;
        this.glyphCodePoint = glyphCodePoint;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public String glyph() {
        return new String(Character.toChars(glyphCodePoint));
    }

    public ResourceLocation texture() {
        return ResourceLocation.fromNamespaceAndPath("aelysiummod", "textures/font/badges/" + id + ".png");
    }

    public static TipoInsignia fromId(String id) {
        for (TipoInsignia b : values()) {
            if (b.id.equalsIgnoreCase(id)) return b;
        }
        return null;
    }

    private static final Map<TipoRaca, TipoInsignia> RACE_BADGES = new HashMap<>();

    static {
        RACE_BADGES.put(TipoRaca.HUMANO, HUMANO);
        RACE_BADGES.put(TipoRaca.ELVARIN, ELVARIN);
        RACE_BADGES.put(TipoRaca.DRACONO, DRACONO);
        RACE_BADGES.put(TipoRaca.TIEFLING, TIEFLING);
        RACE_BADGES.put(TipoRaca.UNDYNE, UNDYNE);
        RACE_BADGES.put(TipoRaca.VALKYRIA, VALKYRIA);
    }

    public static TipoInsignia forRace(TipoRaca race) {
        return RACE_BADGES.get(race);
    }
}
