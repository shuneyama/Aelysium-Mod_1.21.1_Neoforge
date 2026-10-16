package net.aelysium.aelysiummod.sistemas.admin.protecao.regiao;

import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GerenciadorSelecao {

    private static final Map<UUID, BlockPos> posicao1 = new ConcurrentHashMap<>();
    private static final Map<UUID, BlockPos> posicao2 = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> etapaSelecaoRapida = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> ultimoCliqueSelecaoRapida = new ConcurrentHashMap<>();
    private static final long COOLDOWN_CLIQUE_MS = 2000;

    public static void setPosicao1(UUID jogador, BlockPos pos) {
        posicao1.put(jogador, pos);
    }

    public static void setPosicao2(UUID jogador, BlockPos pos) {
        posicao2.put(jogador, pos);
    }

    public static BlockPos getPosicao1(UUID jogador) {
        return posicao1.get(jogador);
    }

    public static BlockPos getPosicao2(UUID jogador) {
        return posicao2.get(jogador);
    }

    public static boolean temSelecaoCompleta(UUID jogador) {
        return posicao1.containsKey(jogador) && posicao2.containsKey(jogador);
    }

    public static BlockPos getMinimo(UUID jogador) {
        BlockPos p1 = posicao1.get(jogador);
        BlockPos p2 = posicao2.get(jogador);
        if (p1 == null || p2 == null) return null;
        return new BlockPos(
                Math.min(p1.getX(), p2.getX()),
                Math.min(p1.getY(), p2.getY()),
                Math.min(p1.getZ(), p2.getZ())
        );
    }

    public static BlockPos getMaximo(UUID jogador) {
        BlockPos p1 = posicao1.get(jogador);
        BlockPos p2 = posicao2.get(jogador);
        if (p1 == null || p2 == null) return null;
        return new BlockPos(
                Math.max(p1.getX(), p2.getX()),
                Math.max(p1.getY(), p2.getY()),
                Math.max(p1.getZ(), p2.getZ())
        );
    }

    public static void limpar(UUID jogador) {
        posicao1.remove(jogador);
        posicao2.remove(jogador);
        etapaSelecaoRapida.remove(jogador);
        ultimoCliqueSelecaoRapida.remove(jogador);
    }

    public static void iniciarSelecaoRapida(UUID jogador) {
        posicao1.remove(jogador);
        posicao2.remove(jogador);
        etapaSelecaoRapida.put(jogador, 1);
        ultimoCliqueSelecaoRapida.remove(jogador);
    }

    public static boolean isAguardandoSelecaoRapida(UUID jogador) {
        return etapaSelecaoRapida.containsKey(jogador);
    }

    public static int avancarEtapaSelecaoRapida(UUID jogador, BlockPos pos) {
        long agora = System.currentTimeMillis();
        Long ultimo = ultimoCliqueSelecaoRapida.get(jogador);
        if (ultimo != null && agora - ultimo < COOLDOWN_CLIQUE_MS) {
            return 0;
        }
        ultimoCliqueSelecaoRapida.put(jogador, agora);

        int etapa = etapaSelecaoRapida.getOrDefault(jogador, 1);
        if (etapa == 1) {
            setPosicao1(jogador, pos);
            etapaSelecaoRapida.put(jogador, 2);
            return 1;
        } else {
            setPosicao2(jogador, pos);
            etapaSelecaoRapida.remove(jogador);
            return 2;
        }
    }
}
