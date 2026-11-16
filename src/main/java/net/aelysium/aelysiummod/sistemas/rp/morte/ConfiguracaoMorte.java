package net.aelysium.aelysiummod.sistemas.rp.morte;

public class ConfiguracaoMorte {
    private static boolean habilitada = false;

    public static boolean isHabilitada() {
        return habilitada;
    }

    public static void setHabilitada(boolean valor) {
        habilitada = valor;
        LimiteTotens.reiniciar();
    }
}
