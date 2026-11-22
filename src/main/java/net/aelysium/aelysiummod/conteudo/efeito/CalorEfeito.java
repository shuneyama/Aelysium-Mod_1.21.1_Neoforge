package net.aelysium.aelysiummod.conteudo.efeito;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class CalorEfeito extends EfeitoSemLeite {

    public CalorEfeito() {
        super(MobEffectCategory.HARMFUL, 0xFF8A1F);
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
