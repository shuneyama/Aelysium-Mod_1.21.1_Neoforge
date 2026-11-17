package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.SonsAnimais;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Pig.class)
public abstract class MixinPorcoSons {

    @Inject(method = "getAmbientSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$ambiente(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Pig) (Object) this, "pig", SonsAnimais.PORCO, "ambient", cir.getReturnValue()));
    }

    @Inject(method = "getHurtSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$dano(DamageSource fonte, CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Pig) (Object) this, "pig", SonsAnimais.PORCO, "hurt", cir.getReturnValue()));
    }

    @Inject(method = "getDeathSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$morte(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Pig) (Object) this, "pig", SonsAnimais.PORCO, "death", cir.getReturnValue()));
    }

    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void aelysium$passo(BlockPos pos, BlockState estado, CallbackInfo ci) {
        Pig animal = (Pig) (Object) this;
        SoundEvent original = SoundEvents.PIG_STEP;
        SoundEvent som = SonsAnimais.trocar(animal, "pig", SonsAnimais.PORCO, "step", original);
        if (som != original) {
            animal.playSound(som, 0.15F, 1.0F);
            ci.cancel();
        }
    }
}
