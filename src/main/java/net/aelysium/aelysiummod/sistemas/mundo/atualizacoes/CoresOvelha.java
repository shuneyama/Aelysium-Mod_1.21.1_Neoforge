package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import net.aelysium.aelysiummod.conteudo.filhotes.clima.ClimaAnimal;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;

public final class CoresOvelha {

    private static final int[] PESOS = {5, 5, 5, 3};

    private CoresOvelha() {
    }

    public static DyeColor sortear(LevelReader mundo, BlockPos pos, RandomSource aleatorio) {
        float temperatura = mundo.getBiome(pos).value().getBaseTemperature();
        byte clima = temperatura >= 0.95F ? ClimaAnimal.QUENTE : temperatura <= 0.3F ? ClimaAnimal.FRIO : ClimaAnimal.TEMPERADO;
        DyeColor padrao = clima == ClimaAnimal.QUENTE ? DyeColor.BROWN : clima == ClimaAnimal.FRIO ? DyeColor.BLACK : DyeColor.WHITE;
        DyeColor[] pequenos = clima == ClimaAnimal.QUENTE
                ? new DyeColor[]{DyeColor.GRAY, DyeColor.LIGHT_GRAY, DyeColor.WHITE, DyeColor.BLACK}
                : clima == ClimaAnimal.FRIO
                ? new DyeColor[]{DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.WHITE, DyeColor.BROWN}
                : new DyeColor[]{DyeColor.BLACK, DyeColor.GRAY, DyeColor.LIGHT_GRAY, DyeColor.BROWN};
        int sorteio = aleatorio.nextInt(100);
        if (sorteio < 18) {
            int acumulado = 0;
            for (int i = 0; i < 4; i++) {
                acumulado += PESOS[i];
                if (sorteio < acumulado) return pequenos[i];
            }
        }
        return aleatorio.nextInt(500) == 0 ? DyeColor.PINK : padrao;
    }
}
