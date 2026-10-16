package net.aelysium.aelysiummod.nucleo.cliente;

import java.util.function.DoubleSupplier;

public final class VolumeSomCliente {

    private static DoubleSupplier fonte = () -> 1.0;

    private VolumeSomCliente() {
    }

    public static void registrar(DoubleSupplier novaFonte) {
        fonte = novaFonte;
    }

    public static double normalizado() {
        return fonte.getAsDouble();
    }
}
