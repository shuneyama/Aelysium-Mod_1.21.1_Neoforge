package net.aelysium.aelysiummod.sistemas.admin.atalhos;

public final class EstadoEspiao {
    private static volatile int nivel;

    private EstadoEspiao() {
    }

    public static int nivel() {
        return nivel;
    }

    public static void definir(int valor) {
        nivel = valor;
    }
}
