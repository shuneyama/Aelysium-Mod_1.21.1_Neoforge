package net.aelysium.aelysiummod.conteudo.filhotes.clima;

import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;

public final class CacheClimaCliente {

    private static final Int2ByteOpenHashMap CLIMAS = new Int2ByteOpenHashMap();

    static {
        CLIMAS.defaultReturnValue((byte) -1);
    }

    private CacheClimaCliente() {
    }

    public static void definir(int entidade, byte clima) {
        CLIMAS.put(entidade, clima);
    }

    public static byte obter(int entidade) {
        return CLIMAS.get(entidade);
    }

    public static void remover(int entidade) {
        CLIMAS.remove(entidade);
    }

    public static void limpar() {
        CLIMAS.clear();
    }
}
