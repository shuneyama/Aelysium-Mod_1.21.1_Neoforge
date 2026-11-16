package net.aelysium.aelysiummod.conteudo.efeito;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ExaustaoAbismoEfeito extends EfeitoSemLeite {

    public ExaustaoAbismoEfeito() {
        super(MobEffectCategory.HARMFUL, 0x0B3D5C);

        this.addAttributeModifier(
                Attributes.MAX_HEALTH,
                ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "exaustao_abismo"),
                -4.0,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}
