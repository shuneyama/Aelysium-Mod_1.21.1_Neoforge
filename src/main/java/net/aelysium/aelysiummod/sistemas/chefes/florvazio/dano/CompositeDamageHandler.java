package net.aelysium.aelysiummod.sistemas.chefes.florvazio.dano;

import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.IEntityStats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Arrays;

public class CompositeDamageHandler implements IDamageHandler {
    private final List<IDamageHandler> handlerList;

    public CompositeDamageHandler(IDamageHandler... handlers) {
        this.handlerList = Arrays.asList(handlers);
    }

    @Override
    public void beforeDamage(IEntityStats stats, DamageSource damageSource, float amount) {
        for (IDamageHandler handler : handlerList)
            handler.beforeDamage(stats, damageSource, amount);
    }

    @Override
    public void afterDamage(IEntityStats stats, DamageSource damageSource, float amount, boolean result) {
        for (IDamageHandler handler : handlerList)
            handler.afterDamage(stats, damageSource, amount, result);
    }

    @Override
    public boolean shouldDamage(LivingEntity actor, DamageSource damageSource, float amount) {
        for (IDamageHandler handler : handlerList)
            if (!handler.shouldDamage(actor, damageSource, amount))
                return false;

        return true;
    }
}
