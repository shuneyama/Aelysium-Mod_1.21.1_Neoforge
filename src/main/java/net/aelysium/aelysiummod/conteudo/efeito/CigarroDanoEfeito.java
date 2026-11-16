package net.aelysium.aelysiummod.conteudo.efeito;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class CigarroDanoEfeito extends EfeitoSemLeite {

    public static final double[] MULTIPLICADORES = {
            1.10, 1.20, 1.30, 1.40, 2.00, 2.20, 2.40, 2.60, 2.80
    };

    public CigarroDanoEfeito() {
        super(MobEffectCategory.HARMFUL, 0x4A2C1D);
    }
}
