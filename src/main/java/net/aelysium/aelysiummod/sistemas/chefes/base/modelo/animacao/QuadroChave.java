package net.aelysium.aelysiummod.sistemas.chefes.base.modelo.animacao;

import net.minecraft.util.Mth;

import java.util.function.DoubleUnaryOperator;

public record QuadroChave(double duracao, double inicio, double fim, DoubleUnaryOperator curva, boolean spline, double splineAntes, double splineDepois) {

    public double valor(double tickNoQuadro) {
        if (tickNoQuadro >= duracao) return fim;
        double progresso = tickNoQuadro / duracao;
        if (spline) return Suavizacao.pontoSpline(progresso, splineAntes, inicio, fim, splineDepois);
        return Mth.lerp(curva.applyAsDouble(progresso), inicio, fim);
    }
}
