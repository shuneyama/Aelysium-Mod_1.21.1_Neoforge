package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.SonsAnimais;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Cat.class)
public abstract class MixinGatoSons {

    @Inject(method = "getAmbientSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$ambiente(CallbackInfoReturnable<SoundEvent> cir) {
        SoundEvent original = cir.getReturnValue();
        String tipo = original == SoundEvents.CAT_PURR ? "purr"
                : original == SoundEvents.CAT_PURREOW ? "purreow"
                : original == SoundEvents.CAT_STRAY_AMBIENT ? "stray_ambient" : "ambient";
        cir.setReturnValue(SonsAnimais.trocar((Cat) (Object) this, "cat", SonsAnimais.GATO, tipo, original));
    }

    @Inject(method = "getHurtSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$dano(DamageSource fonte, CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Cat) (Object) this, "cat", SonsAnimais.GATO, "hurt", cir.getReturnValue()));
    }

    @Inject(method = "getDeathSound", at = @At("RETURN"), cancellable = true)
    private void aelysium$morte(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SonsAnimais.trocar((Cat) (Object) this, "cat", SonsAnimais.GATO, "death", cir.getReturnValue()));
    }

    @Inject(method = "hiss", at = @At("HEAD"), cancellable = true)
    private void aelysium$chiar(CallbackInfo ci) {
        Cat gato = (Cat) (Object) this;
        SoundEvent som = SonsAnimais.trocar(gato, "cat", SonsAnimais.GATO, "hiss", SoundEvents.CAT_HISS);
        if (som != SoundEvents.CAT_HISS) {
            gato.makeSound(som);
            ci.cancel();
        }
    }

    @ModifyArg(method = {"usePlayerItem", "tick", "handleLieDown"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Cat;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"), index = 0)
    private SoundEvent aelysium$tocar(SoundEvent original) {
        String tipo = original == SoundEvents.CAT_EAT ? "eat"
                : original == SoundEvents.CAT_BEG_FOR_FOOD ? "beg_for_food"
                : original == SoundEvents.CAT_PURR ? "purr" : null;
        if (tipo == null) return original;
        return SonsAnimais.trocar((Cat) (Object) this, "cat", SonsAnimais.GATO, tipo, original);
    }
}
