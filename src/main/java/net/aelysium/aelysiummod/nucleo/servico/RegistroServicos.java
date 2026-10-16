package net.aelysium.aelysiummod.nucleo.servico;

public final class RegistroServicos {

    private static ListaNegraMods listaNegraMods;

    private RegistroServicos() {
    }

    public static void registrarListaNegraMods(ListaNegraMods servico) { listaNegraMods = servico; }

    public static ListaNegraMods listaNegraMods() {
        if (listaNegraMods == null) throw new IllegalStateException("RegistroServicos.listaNegraMods nao registrado");
        return listaNegraMods;
    }
}
