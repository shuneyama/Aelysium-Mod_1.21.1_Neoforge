package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.efeito;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class MowzieEffect extends MobEffect {

    public MowzieEffect(MobEffectCategory type, int liquidColor) {
        super(type, liquidColor);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int id, int amplifier) {
        return true;
    }
}
