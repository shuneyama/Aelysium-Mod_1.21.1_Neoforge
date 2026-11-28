package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util;

import net.minecraft.world.effect.MobEffectInstance;

public interface IMobEffectFilter {
    boolean canBeAffected(MobEffectInstance effect);
}
