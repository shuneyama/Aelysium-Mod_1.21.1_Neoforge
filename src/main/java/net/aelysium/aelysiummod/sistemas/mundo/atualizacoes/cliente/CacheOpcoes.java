package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente;

public final class CacheOpcoes {

    private static final long VALIDADE = 250_000_000L;

    private static volatile long ultimo;
    private static volatile boolean mipmapTodos = true;
    private static volatile boolean folhas = true;
    private static volatile int densidadeChuva = 50;
    private static volatile int densidadeNeve = 50;

    private CacheOpcoes() {
    }

    private static void atualizar() {
        long agora = System.nanoTime();
        if (agora - ultimo < VALIDADE) return;
        ultimo = agora;
        mipmapTodos = OpcoesAtualizadas.MIPMAP_TODOS.get();
        folhas = OpcoesAtualizadas.FOLHAS_TRANSPARENTES.get();
        densidadeChuva = OpcoesAtualizadas.DENSIDADE_CHUVA.get();
        densidadeNeve = OpcoesAtualizadas.DENSIDADE_NEVE.get();
    }

    public static boolean mipmapTodos() {
        atualizar();
        return mipmapTodos;
    }

    public static boolean folhas() {
        atualizar();
        return folhas;
    }

    public static int densidadeNeve() {
        atualizar();
        return densidadeNeve;
    }

    public static int densidadeChuva() {
        atualizar();
        return densidadeChuva;
    }
}
