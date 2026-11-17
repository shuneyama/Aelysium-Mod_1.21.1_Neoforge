package net.aelysium.aelysiummod.sistemas.mundo.flora;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class Espalhar {

    private Espalhar() {
    }

    public static boolean possivel(LevelReader mundo, BlockPos pos, BlockState bloco) {
        return procurar(new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList()), mundo, pos, bloco).isPresent();
    }

    public static Optional<BlockPos> encontrar(Level mundo, BlockPos pos, BlockState bloco) {
        List<Direction> direcoes = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
        Collections.shuffle(direcoes, new java.util.Random(mundo.getRandom().nextLong()));
        return procurar(direcoes, mundo, pos, bloco);
    }

    private static Optional<BlockPos> procurar(List<Direction> direcoes, LevelReader mundo, BlockPos pos, BlockState bloco) {
        for (Direction direcao : direcoes) {
            BlockPos vizinho = pos.relative(direcao);
            if (mundo.isEmptyBlock(vizinho) && bloco.canSurvive(mundo, vizinho)) return Optional.of(vizinho);
        }
        return Optional.empty();
    }
}
