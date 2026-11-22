package net.aelysium.aelysiummod.sistemas.mundo.lithynor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public record GradienteZ(double inicio, double fim) implements DensityFunction.SimpleFunction {

    public static final MapCodec<GradienteZ> MAPA = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.DOUBLE.fieldOf("start").forGetter(GradienteZ::inicio),
            Codec.DOUBLE.fieldOf("end").forGetter(GradienteZ::fim)
    ).apply(i, GradienteZ::new));

    public static final KeyDispatchDataCodec<GradienteZ> CODEC = KeyDispatchDataCodec.of(MAPA);

    @Override
    public double compute(FunctionContext contexto) {
        return Mth.clamp((contexto.blockZ() - inicio) / (fim - inicio), 0.0, 1.0);
    }

    @Override
    public double minValue() {
        return 0.0;
    }

    @Override
    public double maxValue() {
        return 1.0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
