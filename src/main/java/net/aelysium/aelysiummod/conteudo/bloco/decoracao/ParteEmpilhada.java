package net.aelysium.aelysiummod.conteudo.bloco.decoracao;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

public enum ParteEmpilhada implements StringRepresentable {
    SOLO("solo"),
    BASE("base"),
    MEIO("meio"),
    TOPO("topo");

    private final String nome;

    ParteEmpilhada(String nome) {
        this.nome = nome;
    }

    @Override
    public String getSerializedName() {
        return nome;
    }

    public static ParteEmpilhada calcular(LevelReader nivel, BlockPos pos, Block bloco) {
        boolean acima = nivel.getBlockState(pos.above()).is(bloco);
        boolean abaixo = nivel.getBlockState(pos.below()).is(bloco);
        if (acima && abaixo) {
            return MEIO;
        }
        if (acima) {
            return BASE;
        }
        if (abaixo) {
            return TOPO;
        }
        return SOLO;
    }
}
