package net.aelysium.aelysiummod.nucleo.cliente;

import net.minecraft.client.gui.screens.Screen;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class TelasCliente {

    private static BiFunction<Screen, Consumer<String>, Screen> seletorInsignia;

    private static java.util.function.Function<Screen, Screen> criadorInsignia;

    private TelasCliente() {
    }

    public static void registrarSeletorInsignia(BiFunction<Screen, Consumer<String>, Screen> fabrica) {
        seletorInsignia = fabrica;
    }

    public static void registrarCriadorInsignia(java.util.function.Function<Screen, Screen> fabrica) {
        criadorInsignia = fabrica;
    }

    public static Screen criadorInsignia(Screen pai) {
        if (criadorInsignia == null) throw new IllegalStateException("TelasCliente.criadorInsignia nao registrado");
        return criadorInsignia.apply(pai);
    }

    public static Screen seletorInsignia(Screen pai, Consumer<String> aoSelecionar) {
        if (seletorInsignia == null) throw new IllegalStateException("TelasCliente.seletorInsignia nao registrado");
        return seletorInsignia.apply(pai, aoSelecionar);
    }
}
