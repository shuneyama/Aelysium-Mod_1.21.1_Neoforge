package net.aelysium.aelysiummod.item.custom;

import net.aelysium.aelysiummod.efeitos.ModEfeitos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class Arma_Aeon extends SwordItem {
    public Arma_Aeon(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        target.addEffect(new MobEffectInstance(
                ModEfeitos.CONGELAMENTO,
                1000000000,
                0,
                false,
                false
        ));

        return super.hurtEnemy(stack, target, attacker);
    }
}
