package net.aelysium.aelysiummod.conteudo.efeito;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class FrioEfeito extends EfeitoSemLeite {

    public FrioEfeito() {
        super(MobEffectCategory.HARMFUL, 0x9BE3FF);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entidade, int amplificador) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duracao, int amplificador) {
        return false;
    }
}
