package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Stack;

@Mixin(LivingEntity.class)
public abstract class MixinOrdemReducaoDano {

    @Shadow protected Stack<DamageContainer> damageContainers;

    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("HEAD"), cancellable = true)
    private void aelysium$protecaoAntesDaResistencia(DamageSource fonte, float dano, CallbackInfoReturnable<Float> cir) {
        LivingEntity entidade = (LivingEntity) (Object) this;
        if (fonte.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            cir.setReturnValue(dano);
            return;
        }
        DamageContainer container = this.damageContainers.isEmpty() ? null : this.damageContainers.peek();

        if (!fonte.is(DamageTypeTags.BYPASSES_ENCHANTMENTS) && dano > 0.0F && entidade.level() instanceof ServerLevel nivel) {
            float protecao = EnchantmentHelper.getDamageProtection(nivel, entidade, fonte);
            if (protecao > 0.0F) {
                float depois = CombatRules.getDamageAfterMagicAbsorb(dano, protecao);
                if (container != null) {
                    container.setReduction(DamageContainer.Reduction.ENCHANTMENTS, dano - depois);
                    dano = container.getNewDamage();
                } else {
                    dano = depois;
                }
            }
        }

        MobEffectInstance resistencia = entidade.getEffect(MobEffects.DAMAGE_RESISTANCE);
        if (resistencia != null && dano > 0.0F && !fonte.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            int reducao = (resistencia.getAmplifier() + 1) * 5;
            float depois = Math.max(dano * (25 - reducao) / 25.0F, 0.0F);
            float resistido = dano - depois;
            if (resistido > 0.0F && resistido < 3.4028235E37F) {
                if (container != null) {
                    container.setReduction(DamageContainer.Reduction.MOB_EFFECTS, resistido);
                    depois = container.getNewDamage();
                }
                if (entidade instanceof ServerPlayer jogador) {
                    jogador.awardStat(Stats.DAMAGE_RESISTED, Math.round(resistido * 10.0F));
                } else if (fonte.getEntity() instanceof ServerPlayer atacante) {
                    atacante.awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(resistido * 10.0F));
                }
            }
            dano = depois;
        }

        cir.setReturnValue(Math.max(dano, 0.0F));
    }
}
