package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.SonsAnimais;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Cow.class)
public abstract class MixinVacaSons {

    @Inject(method = "getAmbientSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$ambiente(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Cow) (Object) this, "cow", SonsAnimais.VACA, "ambient", cir.getReturnValue()));
    }

    @Inject(method = "getHurtSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$dano(DamageSource fonte, CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Cow) (Object) this, "cow", SonsAnimais.VACA, "hurt", cir.getReturnValue()));
    }

    @Inject(method = "getDeathSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$morte(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Cow) (Object) this, "cow", SonsAnimais.VACA, "death", cir.getReturnValue()));
    }

    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void aelysium$passo(BlockPos pos, BlockState estado, CallbackInfo ci) {
        Cow animal = (Cow) (Object) this;
        SoundEvent original = SoundEvents.COW_STEP;
        SoundEvent som = SonsAnimais.trocar(animal, "cow", SonsAnimais.VACA, "step", original);
        if (som != original) {
            animal.playSound(som, 0.15F, 1.0F);
            ci.cancel();
        }
    }
}
