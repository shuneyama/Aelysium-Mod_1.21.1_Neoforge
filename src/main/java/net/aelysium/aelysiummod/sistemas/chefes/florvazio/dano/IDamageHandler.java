package net.aelysium.aelysiummod.sistemas.chefes.florvazio.dano;

import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.IEntityStats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public interface IDamageHandler {
    void beforeDamage(IEntityStats stats, DamageSource damageSource, float amount);
    void afterDamage(IEntityStats stats, DamageSource damageSource, float amount, boolean result);
    boolean shouldDamage(LivingEntity actor, DamageSource damageSource, float amount);
}
