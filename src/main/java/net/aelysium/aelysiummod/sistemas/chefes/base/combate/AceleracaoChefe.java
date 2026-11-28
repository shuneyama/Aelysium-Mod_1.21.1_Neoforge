package net.aelysium.aelysiummod.sistemas.chefes.base.combate;

import net.minecraft.world.entity.LivingEntity;

public final class AceleracaoChefe {

    private AceleracaoChefe() {
    }

    public static int fasesPerdidas(LivingEntity chefe) {
        float maxima = chefe.getMaxHealth();
        if (maxima <= 0) return 0;
        float perdida = 1.0F - chefe.getHealth() / maxima;
        return Math.max(0, Math.min(3, (int) Math.floor(perdida * 4.0F + 1.0E-4F)));
    }

    public static double multiplicador(LivingEntity chefe, String chaveChefe) {
        double porFase = Math.max(0.0, ConfiguracaoChefes.numero(chaveChefe + ".aceleracao_por_fase"));
        return 1.0 + porFase * fasesPerdidas(chefe);
    }

    public static int recarga(LivingEntity chefe, String chaveChefe, int base, int minimo) {
        if (base <= 0) return base;
        int acelerada = (int) Math.round(base / multiplicador(chefe, chaveChefe));
        return Math.max(Math.max(acelerada, minimo), 1);
    }
}
