package net.aelysium.aelysiummod.sistemas.chefes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.util.CompoundOrientedBox;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VoxelShape.class)
public class MixinFormaVoxelMultiparte {

    @ModifyReturnValue(method = "collide(Lnet/minecraft/core/Direction$Axis;Lnet/minecraft/world/phys/AABB;D)D", at = @At("RETURN"))
    private double aelysium$colidir(double original, @Local(argsOnly = true) Direction.Axis eixo, @Local(argsOnly = true) AABB caixa, @Local(argsOnly = true) double distancia) {
        return caixa instanceof CompoundOrientedBox composta ? composta.calculateMaxDistance(eixo, (VoxelShape) (Object) this, distancia) : original;
    }
}
