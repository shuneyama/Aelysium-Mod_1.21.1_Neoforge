package net.aelysium.aelysiummod.conteudo.efeito;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ArmaduraRachadaEfeito extends MobEffect {

    public ArmaduraRachadaEfeito() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);

        this.addAttributeModifier(
                Attributes.ARMOR,
                ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "armadura_rachada"),
                -4.0,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}
