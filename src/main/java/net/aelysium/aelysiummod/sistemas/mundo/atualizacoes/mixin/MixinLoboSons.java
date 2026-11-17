package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.flora.SonsExtras;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Wolf.class)
public abstract class MixinLoboSons {

    private SoundEvent aelysium$som(Wolf lobo, SoundEvent original, String tipo) {
        if (lobo.isBaby()) {
            SoundEvent filhote = SonsExtras.FILHOTE.get(tipo);
            return filhote != null ? filhote : original;
        }
        int indice = Math.floorMod(lobo.getUUID().hashCode(), SonsExtras.VARIANTES_LOBO.length);
        Map<String, SoundEvent> sons = SonsExtras.LOBOS.get(SonsExtras.VARIANTES_LOBO[indice]);
        if (sons == null) return original;
        SoundEvent som = sons.get(tipo);
        return som != null ? som : original;
    }

    @Inject(method = "getAmbientSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$ambiente(CallbackInfoReturnable<SoundEvent> cir) {
        Wolf lobo = (Wolf) (Object) this;
        SoundEvent original = cir.getReturnValue();
        String tipo = original == net.minecraft.sounds.SoundEvents.WOLF_GROWL ? "growl"
                : original == net.minecraft.sounds.SoundEvents.WOLF_WHINE ? "whine"
                : original == net.minecraft.sounds.SoundEvents.WOLF_PANT ? "pant" : "ambient";
        cir.setReturnValue(aelysium$som(lobo, original, tipo));
    }

    @Inject(method = "getHurtSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$dano(DamageSource fonte, CallbackInfoReturnable<SoundEvent> cir) {
        if (cir.getReturnValue() == net.minecraft.sounds.SoundEvents.WOLF_HURT) {
            cir.setReturnValue(aelysium$som((Wolf) (Object) this, cir.getReturnValue(), "hurt"));
        }
    }

    @Inject(method = "getDeathSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$morte(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(aelysium$som((Wolf) (Object) this, cir.getReturnValue(), "death"));
    }

    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void aelysium$passo(BlockPos pos, BlockState estado, CallbackInfo ci) {
        Wolf lobo = (Wolf) (Object) this;
        if (lobo.isBaby()) {
            lobo.playSound(SonsExtras.FILHOTE.get("step"), 0.15F, 1.0F);
            ci.cancel();
        }
    }
}
