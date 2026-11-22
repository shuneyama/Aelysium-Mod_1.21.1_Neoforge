package net.aelysium.aelysiummod.sistemas.mundo.lithynor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public record DistanciaCentral(double inicio, double fim) implements DensityFunction.SimpleFunction {

    public static final MapCodec<DistanciaCentral> MAPA = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.DOUBLE.fieldOf("start").forGetter(DistanciaCentral::inicio),
            Codec.DOUBLE.fieldOf("end").forGetter(DistanciaCentral::fim)
    ).apply(i, DistanciaCentral::new));

    public static final KeyDispatchDataCodec<DistanciaCentral> CODEC = KeyDispatchDataCodec.of(MAPA);

    @Override
    public double compute(FunctionContext contexto) {
        double x = contexto.blockX();
        double z = contexto.blockZ();
        double distancia = Math.sqrt(x * x + z * z);
        return Mth.clamp((distancia - inicio) / (fim - inicio), 0.0, 1.0);
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
