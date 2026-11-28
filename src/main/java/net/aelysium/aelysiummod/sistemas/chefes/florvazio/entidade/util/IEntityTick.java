package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util;

import net.minecraft.world.level.Level;

public interface IEntityTick<T extends Level> {
    void tick(T level);
}
