package net.aelysium.aelysiummod.raca;

public enum RaceType {

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

    RaceType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public static RaceType fromId(String id) {
        for (RaceType r : values()) {
            if (r.id.equalsIgnoreCase(id)) return r;
        }
        return NONE;
    }
}