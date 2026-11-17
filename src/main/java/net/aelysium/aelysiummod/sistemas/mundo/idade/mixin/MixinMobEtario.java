package net.aelysium.aelysiummod.sistemas.mundo.idade.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.idade.TravaIdade;
import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableMob.class)
public abstract class MixinMobEtario {

    @Shadow
    protected int age;

    @Inject(method = "setAge", at = @At("HEAD"), cancellable = true)
    private void aelysium$travarIdade(int novaIdade, CallbackInfo ci) {
        if (this.age < 0 && TravaIdade.deveBloquearCrescimento((AgeableMob) (Object) this, this.age, novaIdade)) {
            ci.cancel();
        }
    }
}
