package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.FlowerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FlowerBlock.class)
public abstract class MixinFlor {

    @ModifyVariable(method = "makeEffectList", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static float aelysium$duracaoSopa(float segundos, Holder<MobEffect> efeito) {
        if (efeito.is(MobEffects.FIRE_RESISTANCE)) return 3.0F;
        if (efeito.is(MobEffects.BLINDNESS)) return 11.0F;
        if (efeito.is(MobEffects.WEAKNESS)) return 7.0F;
        if (efeito.is(MobEffects.REGENERATION)) return 7.0F;
        if (efeito.is(MobEffects.JUMP)) return 5.0F;
        if (efeito.is(MobEffects.WITHER)) return 7.0F;
        if (efeito.is(MobEffects.POISON)) return 11.0F;
        return segundos;
    }
}
