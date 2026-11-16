package net.aelysium.aelysiummod.nucleo.texto;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.CommandNode;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public final class SemConversao {

    private static final ThreadLocal<int[]> NIVEL = ThreadLocal.withInitial(() -> new int[1]);
    private static final Field CAMPO_COMANDO = localizarCampo();

    private SemConversao() {}

    private static Field localizarCampo() {
        try {
            Field campo = CommandNode.class.getDeclaredField("command");
            campo.setAccessible(true);
            return campo;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    public static boolean ativo() {
        return NIVEL.get()[0] > 0;
    }

    public static void executar(Runnable acao) {
        int[] nivel = NIVEL.get();
        nivel[0]++;
        try {
            acao.run();
        } finally {
            nivel[0]--;
        }
    }

    public static <S> void proteger(CommandNode<S> no) {
        proteger(no, new HashSet<>());
    }

    private static <S> void proteger(CommandNode<S> no, Set<CommandNode<S>> visitados) {
        if (no == null || !visitados.add(no)) return;
        Command<S> original = no.getCommand();
        if (original != null && CAMPO_COMANDO != null) {
            try {
                CAMPO_COMANDO.set(no, (Command<S>) contexto -> {
                    int[] nivel = NIVEL.get();
                    nivel[0]++;
                    try {
                        return original.run(contexto);
                    } finally {
                        nivel[0]--;
                    }
                });
            } catch (IllegalAccessException ignored) {
            }
        }
        for (CommandNode<S> filho : no.getChildren()) {
            proteger(filho, visitados);
        }
    }
}
