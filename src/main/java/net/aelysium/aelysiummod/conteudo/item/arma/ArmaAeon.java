package net.aelysium.aelysiummod.conteudo.item.arma;

import net.aelysium.aelysiummod.registro.ModEfeitos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class ArmaAeon extends SwordItem {
    public ArmaAeon(Tier tier, Properties properties) {
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
