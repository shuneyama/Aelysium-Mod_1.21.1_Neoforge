package net.aelysium.aelysiummod.sistemas.social.apelido;

public enum Pronome {

    ELE("ele/dele", "ele/dele", "ele/dele", 0xE74C4C),
    ELA("ela/dela", "ela/dela", "ela/dela", 0x9B5DE5),
    ELU("elu/delu", "elu/delu", "elu/delu", 0x4FD1A5),
    QUALQUER("qualquer", "qualquer", "qualquer pronome", 0xF2D157);

    public final String id;
    public final String comando;
    public final String texto;
    public final int cor;

    Pronome(String id, String comando, String texto, int cor) {
        this.id = id;
        this.comando = comando;
        this.texto = texto;
        this.cor = cor;
    }

    public static Pronome deId(String id) {
        if (id == null) return null;
        for (Pronome p : values()) {
            if (p.id.equals(id)) return p;
        }
        return null;
    }

    public static String proximoId(String idAtual) {
        Pronome atual = deId(idAtual);
        if (atual == null) return values()[0].id;
        int proximo = atual.ordinal() + 1;
        return proximo >= values().length ? "" : values()[proximo].id;
    }
}
