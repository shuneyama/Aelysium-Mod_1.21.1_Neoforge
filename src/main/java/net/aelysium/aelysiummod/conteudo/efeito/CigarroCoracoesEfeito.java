package net.aelysium.aelysiummod.conteudo.efeito;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CigarroCoracoesEfeito extends MobEffect {

    public CigarroCoracoesEfeito() {
        super(MobEffectCategory.BENEFICIAL, 0xC77B4A);

        this.addAttributeModifier(
                Attributes.MAX_HEALTH,
                ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "cigarro_coracoes_max_health"),
                2.0,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}
