package net.aelysium.aelysiummod.sistemas.social.maozinha;

public enum FaseMaozinha {
    DESFEITA,
    OFERTA,
    SEGURANDO;

    public static FaseMaozinha porOrdem(int ordem) {
        FaseMaozinha[] valores = values();
        return ordem >= 0 && ordem < valores.length ? valores[ordem] : DESFEITA;
    }
}
