package net.aelysium.aelysiummod.nucleo.jogador;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TipoDeus {

    private static final List<TipoDeus> REGISTRO = new CopyOnWriteArrayList<>();

    public static final TipoDeus NONE = interno("none", "Nenhum", 0x000000);
    public static final TipoDeus KAIROS = interno("kairos", "Kairos", 0xFFD700);
    public static final TipoDeus RONOVA = interno("ronova", "Ronova", 0xE02424);
    public static final TipoDeus AZARUS = interno("azarus", "Azarus", 0x006994);
    public static final TipoDeus YANCY = interno("yancy", "Yancy", 0x87CEEB);
    public static final TipoDeus ASHYRA = interno("ashyra", "Ashyra", 0xFF8C00);
    public static final TipoDeus EREBOS = interno("erebos", "Erebos", 0x8B008B);
    public static final TipoDeus PHANES = interno("phanes", "Phanes", 0xF0F0F0);
    public static final TipoDeus KLAUS = interno("klaus", "Klaus", 0x00CED1);
    public static final TipoDeus FREYR = interno("freyr", "Freyr", 0x32CD32);

    public final String id;
    public final String displayName;
    public final int color;
    public final boolean personalizado;

    private TipoDeus(String id, String displayName, int color, boolean personalizado) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.personalizado = personalizado;
    }

    private static TipoDeus interno(String id, String displayName, int color) {
        TipoDeus deus = new TipoDeus(id, displayName, color, false);
        REGISTRO.add(deus);
        return deus;
    }

    public static synchronized TipoDeus registrarPersonalizado(String id, String displayName, int color) {
        if (fromId(id) != NONE || NONE.id.equalsIgnoreCase(id)) return null;
        TipoDeus deus = new TipoDeus(id, displayName, color & 0xFFFFFF, true);
        REGISTRO.add(deus);
        return deus;
    }

    public static synchronized void limparPersonalizados() {
        REGISTRO.removeIf(deus -> deus.personalizado);
    }

    public static TipoDeus[] values() {
        return REGISTRO.toArray(new TipoDeus[0]);
    }

    public static TipoDeus fromId(String id) {
        for (TipoDeus d : REGISTRO) {
            if (d.id.equalsIgnoreCase(id)) return d;
        }
        return NONE;
    }
}
