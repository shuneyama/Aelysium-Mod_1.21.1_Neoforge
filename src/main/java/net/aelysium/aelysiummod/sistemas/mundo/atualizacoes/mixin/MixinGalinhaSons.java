package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.SonsAnimais;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Chicken.class)
public abstract class MixinGalinhaSons {

    @Inject(method = "getAmbientSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$ambiente(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Chicken) (Object) this, "chicken", SonsAnimais.GALINHA, "ambient", cir.getReturnValue()));
    }

    @Inject(method = "getHurtSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$dano(DamageSource fonte, CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Chicken) (Object) this, "chicken", SonsAnimais.GALINHA, "hurt", cir.getReturnValue()));
    }

    @Inject(method = "getDeathSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$morte(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Chicken) (Object) this, "chicken", SonsAnimais.GALINHA, "death", cir.getReturnValue()));
    }

    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void aelysium$passo(BlockPos pos, BlockState estado, CallbackInfo ci) {
        Chicken animal = (Chicken) (Object) this;
        SoundEvent original = SoundEvents.CHICKEN_STEP;
        SoundEvent som = SonsAnimais.trocar(animal, "chicken", SonsAnimais.GALINHA, "step", original);
        if (som != original) {
            animal.playSound(som, 0.15F, 1.0F);
            ci.cancel();
        }
    }
}
