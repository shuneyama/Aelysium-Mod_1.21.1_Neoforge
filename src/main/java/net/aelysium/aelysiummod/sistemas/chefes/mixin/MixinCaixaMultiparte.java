package net.aelysium.aelysiummod.sistemas.chefes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.util.CompoundOrientedBox;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AABB.class)
public class MixinCaixaMultiparte {

    @ModifyReturnValue(method = "intersects(Lnet/minecraft/world/phys/AABB;)Z", at = @At("RETURN"))
    private boolean aelysium$intersecta(boolean original, @Local(argsOnly = true) AABB caixa) {
        return caixa instanceof CompoundOrientedBox composta ? composta.intersects((AABB) (Object) this) : original;
    }
}
