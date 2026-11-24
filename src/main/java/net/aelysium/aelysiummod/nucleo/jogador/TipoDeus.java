package net.aelysium.aelysiummod.nucleo.jogador;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TipoDeus {

    private static final List<TipoDeus> REGISTRO = new CopyOnWriteArrayList<>();
    private static final List<TipoDeus> ORIGINAIS = new CopyOnWriteArrayList<>();

    public static final TipoDeus NONE = interno("none", "Nenhum", 0x000000);
    public static final TipoDeus KAIROS = interno("kairos", "Kairos", 0xFFD217);
    public static final TipoDeus RONOVA = interno("ronova", "Ronova", 0xE02424, 0x000000);
    public static final TipoDeus AZARUS = interno("azarus", "Azarus", 0x77B1D4);
    public static final TipoDeus YANCY = interno("yancy", "Yancy", 0x9BC2F9);
    public static final TipoDeus ASHYRA = interno("ashyra", "Ashyra", 0xFF3800);
    public static final TipoDeus EREBOS = interno("erebos", "Erebos", 0x8B008B);
    public static final TipoDeus PHANES = interno("phanes", "Phanes", 0xF0F0F0);
    public static final TipoDeus KLAUS = interno("klaus", "Klaus", 0x00CED1);
    public static final TipoDeus FREYR = interno("freyr", "Freyr", 0x32CD32);

    public static final int INVULNERAVEL = 1;
    public static final int VIDA_ALTA = 2;
    public static final int PARTICULA = 4;
    public static final int DANO_MORTAL = 8;
    public static final int VOO = 16;
    public static final int SEM_FOME = 32;
    public static final int TODOS_PODERES = 63;

    public final String id;
    public volatile String displayName;
    public volatile int color;
    public volatile String badge;
    public volatile int corParticula;
    public volatile String pronomes;
    public volatile int poderes;
    public final boolean personalizado;
    private final String nomePadrao;
    private final int corPadrao;
    private final String badgePadrao;
    private final int corParticulaPadrao;
    private final String pronomesPadrao;
    private final int poderesPadrao;

    private TipoDeus(String id, String displayName, int color, boolean personalizado, int corParticula) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.personalizado = personalizado;
        this.nomePadrao = displayName;
        this.corPadrao = color;
        this.badge = TipoInsignia.DEUS.id;
        this.badgePadrao = this.badge;
        this.corParticula = corParticula;
        this.corParticulaPadrao = corParticula;
        this.pronomes = personalizado ? "" : pronomesOriginais(id);
        this.pronomesPadrao = this.pronomes;
        this.poderes = personalizado ? 0 : TODOS_PODERES;
        this.poderesPadrao = this.poderes;
    }

    public boolean tem(int poder) {
        return (poderes & poder) != 0;
    }

    private static String pronomesOriginais(String id) {
        return switch (id) {
            case "ronova", "azarus", "ashyra", "yancy" -> "ela/dela";
            case "kairos", "freyr", "klaus" -> "ele/dele";
            default -> "";
        };
    }

    private static TipoDeus interno(String id, String displayName, int color) {
        return interno(id, displayName, color, color);
    }

    private static TipoDeus interno(String id, String displayName, int color, int corParticula) {
        TipoDeus deus = new TipoDeus(id, displayName, color, false, corParticula);
        REGISTRO.add(deus);
        ORIGINAIS.add(deus);
        return deus;
    }

    public static synchronized TipoDeus registrarPersonalizado(String id, String displayName, int color, String badge, int corParticula, String pronomes, int poderes) {
        if (fromId(id) != NONE || NONE.id.equalsIgnoreCase(id)) return null;
        TipoDeus deus = new TipoDeus(id, displayName, color & 0xFFFFFF, true, color & 0xFFFFFF);
        deus.badge = badge == null ? TipoInsignia.DEUS.id : badge;
        deus.corParticula = corParticula < 0 ? deus.color : corParticula & 0xFFFFFF;
        deus.pronomes = pronomes == null ? "" : pronomes;
        deus.poderes = poderes & TODOS_PODERES;
        REGISTRO.add(deus);
        return deus;
    }

    public static synchronized boolean atualizar(String id, String displayName, int color, String badge, int corParticula, String pronomes, int poderes) {
        TipoDeus deus = fromId(id);
        if (deus == NONE) return false;
        deus.displayName = displayName;
        deus.color = color & 0xFFFFFF;
        if (badge != null) deus.badge = badge;
        if (corParticula >= 0) deus.corParticula = corParticula & 0xFFFFFF;
        if (pronomes != null) deus.pronomes = pronomes;
        deus.poderes = poderes & TODOS_PODERES;
        return true;
    }

    public static synchronized boolean remover(String id) {
        TipoDeus deus = fromId(id);
        if (deus == NONE) return false;
        return REGISTRO.remove(deus);
    }

    public static boolean removido(TipoDeus original) {
        return !REGISTRO.contains(original);
    }

    public static boolean editado(TipoDeus deus) {
        return !deus.displayName.equals(deus.nomePadrao) || deus.color != deus.corPadrao || !deus.badge.equals(deus.badgePadrao) || deus.corParticula != deus.corParticulaPadrao || !deus.pronomes.equals(deus.pronomesPadrao) || deus.poderes != deus.poderesPadrao;
    }

    public static List<TipoDeus> originais() {
        return List.copyOf(ORIGINAIS);
    }

    public static synchronized void limparPersonalizados() {
        REGISTRO.clear();
        REGISTRO.addAll(ORIGINAIS);
        for (TipoDeus deus : ORIGINAIS) {
            deus.displayName = deus.nomePadrao;
            deus.color = deus.corPadrao;
            deus.badge = deus.badgePadrao;
            deus.corParticula = deus.corParticulaPadrao;
            deus.pronomes = deus.pronomesPadrao;
            deus.poderes = deus.poderesPadrao;
        }
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
