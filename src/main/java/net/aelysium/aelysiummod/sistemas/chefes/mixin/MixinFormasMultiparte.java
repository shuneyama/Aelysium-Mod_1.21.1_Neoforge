package net.aelysium.aelysiummod.sistemas.chefes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.util.CompoundOrientedBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Shapes.class)
public class MixinFormasMultiparte {

    @ModifyReturnValue(method = "create(Lnet/minecraft/world/phys/AABB;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("RETURN"))
    private static VoxelShape aelysium$criar(VoxelShape original, @Local(argsOnly = true) AABB caixa) {
        return caixa instanceof CompoundOrientedBox composta ? composta.toVoxelShape() : original;
    }
}
