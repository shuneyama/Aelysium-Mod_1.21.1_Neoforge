package net.aelysium.aelysiummod.nucleo.jogador;

public enum TipoRaca {

    NONE("none", "Nenhuma"),
    HUMANO("humano", "Humano"),
    ELVARIN("elvarin", "Elvarin"),
    DRACONO("dracono", "Dracono"),
    TIEFLING("tiefling", "Tiefling"),
    UNDYNE("undyne", "Undyne"),
    VALKYRIA("valkyria", "Valkyria"),
    ROBO("robo", "Robô");

    public final String id;
    public final String displayName;

    TipoRaca(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public static TipoRaca fromId(String id) {
        for (TipoRaca r : values()) {
            if (r.id.equalsIgnoreCase(id)) return r;
        }
        return NONE;
    }
}