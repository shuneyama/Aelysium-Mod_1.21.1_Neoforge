package net.aelysium.aelysiummod.sistemas.chefes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade.MultipartAwareEntity;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade.MultipartEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntidadeMultiparte {

    @ModifyReturnValue(method = "getBoundingBox()Lnet/minecraft/world/phys/AABB;", at = @At("RETURN"))
    private AABB aelysium$caixaComposta(AABB original) {
        return (Object) this instanceof MultipartEntity multiparte ? multiparte.getCompoundBoundingBox(original) : original;
    }

    @Inject(method = "setPosRaw(DDD)V", at = @At("TAIL"))
    private void aelysium$aoMover(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof MultipartAwareEntity multiparte) {
            multiparte.onSetPos(x, y, z);
        }
    }
}
