package net.aelysium.aelysiummod.sistemas.chefes.mixin;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArrayVoxelShape.class)
public interface InvocadorFormaVoxelArray {

    @Invoker("<init>")
    static ArrayVoxelShape aelysium$criar(DiscreteVoxelShape forma, DoubleList x, DoubleList y, DoubleList z) {
        throw new AssertionError();
    }
}
