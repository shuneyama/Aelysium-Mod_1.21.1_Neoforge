package net.aelysium.aelysiummod.protecao.regiao;

import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SelecaoManager {

    private static final Map<UUID, BlockPos> posicao1 = new ConcurrentHashMap<>();
    private static final Map<UUID, BlockPos> posicao2 = new ConcurrentHashMap<>();

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
    }
}
