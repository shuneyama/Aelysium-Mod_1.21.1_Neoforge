package net.aelysium.aelysiummod.sistemas.social.maozinha.mixin;

import net.aelysium.aelysiummod.sistemas.social.maozinha.FormacaoMaozinha;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class EntidadeVivaAnimacaoMaozinhaMixin {

    @Inject(method = "calculateEntityAnimation(Z)V", at = @At("HEAD"), cancellable = true)
    private void aelysium_animacaoDoSeguidor(boolean incluirAltura, CallbackInfo ci) {
        if (FormacaoMaozinha.ehSeguidor((LivingEntity) (Object) this)) ci.cancel();
    }
}
