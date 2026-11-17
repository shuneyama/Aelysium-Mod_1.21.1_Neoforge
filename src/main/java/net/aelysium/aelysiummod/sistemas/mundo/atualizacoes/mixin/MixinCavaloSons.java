package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.SonsAnimais;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Horse.class)
public abstract class MixinCavaloSons {

    @Inject(method = "getAmbientSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$ambiente(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Horse) (Object) this, "horse", List.of(), "ambient", cir.getReturnValue()));
    }

    @Inject(method = "getHurtSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$dano(DamageSource fonte, CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Horse) (Object) this, "horse", List.of(), "hurt", cir.getReturnValue()));
    }

    @Inject(method = "getDeathSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$morte(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Horse) (Object) this, "horse", List.of(), "death", cir.getReturnValue()));
    }

    @Inject(method = "getEatingSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$comer(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Horse) (Object) this, "horse", List.of(), "eat", cir.getReturnValue()));
    }

    @Inject(method = "getAngrySound", at = @At("RETURN"), cancellable = true)
    private void aelysium$raiva(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Horse) (Object) this, "horse", List.of(), "angry", cir.getReturnValue()));
    }

    @ModifyArg(method = "playGallopSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/Horse;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"), index = 0)
    private SoundEvent aelysium$respirar(SoundEvent original) {
        return SonsAnimais.trocar((Horse) (Object) this, "horse", List.of(), "breathe", original);
    }
}
