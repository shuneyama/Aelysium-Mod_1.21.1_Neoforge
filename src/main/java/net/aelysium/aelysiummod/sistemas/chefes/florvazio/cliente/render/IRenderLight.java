package net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public interface IRenderLight<T extends Entity> {
    int getBlockLight(T entity, BlockPos blockPos);
}
