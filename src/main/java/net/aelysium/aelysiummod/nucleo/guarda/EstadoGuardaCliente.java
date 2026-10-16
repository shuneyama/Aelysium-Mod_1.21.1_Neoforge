package net.aelysium.aelysiummod.nucleo.guarda;

public class EstadoGuardaCliente {

    private static boolean autorizado = false;

    public static void set(boolean valor) {
        autorizado = valor;
    }

    public static boolean isAutorizado() {
        return autorizado;
    }
}
