package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util;

import net.minecraft.nbt.CompoundTag;

public interface INbtHandler {
    CompoundTag toTag(CompoundTag tag);
    void fromTag(CompoundTag tag);
}
