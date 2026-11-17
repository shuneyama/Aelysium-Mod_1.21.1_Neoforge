package net.aelysium.aelysiummod.sistemas.mundo.idade.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.idade.TravaIdade;
import net.minecraft.world.entity.animal.frog.Tadpole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Tadpole.class)
public abstract class MixinGirino {

    @Shadow
    private int age;

    @Inject(method = "setAge", at = @At("HEAD"), cancellable = true)
    private void aelysium$travarIdade(int novaIdade, CallbackInfo ci) {
        if (TravaIdade.deveBloquearCrescimento((Tadpole) (Object) this, this.age, novaIdade)) {
            ci.cancel();
        }
    }
}
