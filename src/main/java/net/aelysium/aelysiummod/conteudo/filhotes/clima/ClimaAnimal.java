package net.aelysium.aelysiummod.conteudo.filhotes.clima;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;

public final class ClimaAnimal {

    public static final String CHAVE = "AelysiumClima";
    public static final byte TEMPERADO = 0;
    public static final byte FRIO = 1;
    public static final byte QUENTE = 2;

    private ClimaAnimal() {
    }

    public static boolean aplicavel(Entity entidade) {
        if (entidade instanceof MushroomCow) return false;
        return entidade instanceof Cow || entidade instanceof Pig || entidade instanceof Chicken;
    }

    public static byte calcular(Level nivel, BlockPos posicao) {
        float temperatura = nivel.getBiome(posicao).value().getBaseTemperature();
        if (temperatura >= 0.95F) return QUENTE;
        if (temperatura <= 0.3F) return FRIO;
        return TEMPERADO;
    }

    public static byte garantir(Entity entidade) {
        CompoundTag dados = entidade.getPersistentData();
        if (dados.contains(CHAVE)) return dados.getByte(CHAVE);
        byte clima = calcular(entidade.level(), entidade.blockPosition());
        dados.putByte(CHAVE, clima);
        return clima;
    }
}
