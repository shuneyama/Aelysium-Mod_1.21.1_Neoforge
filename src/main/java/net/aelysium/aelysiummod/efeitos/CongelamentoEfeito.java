package net.aelysium.aelysiummod.efeitos;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CongelamentoEfeito extends MobEffect {
    public CongelamentoEfeito(MobEffectCategory category, int color) {
        super(category, color);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "congelamento_speed"),
                -1.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        this.addAttributeModifier(
                Attributes.JUMP_STRENGTH,
                ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "congelamento_jump"),
                -1.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
