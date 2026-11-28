package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util;

import net.minecraft.network.syncher.EntityDataAccessor;

public interface IDataAccessorHandler {
    void onSyncedDataUpdated(EntityDataAccessor<?> data);
}
