package io.github.kosmx.bendylib;

import com.mojang.datafixers.util.Pair;
import io.github.kosmx.bendylib.impl.ICuboid;

import javax.annotation.Nullable;

public interface MutableCuboid {

    boolean registerMutator(String name, ICuboidBuilder<ICuboid> builder);

    boolean unregisterMutator(String name);

    @Nullable
    Pair<String, ICuboid> getActiveMutator();

    boolean hasMutator(String key);

    @Nullable
    ICuboidBuilder<ICuboid> getCuboidBuilder(String key);

    @Nullable
    @Deprecated
    ICuboid getMutator(String name);

    @Nullable
    ICuboid getAndActivateMutator(@Nullable String name);

    void copyStateFrom(MutableCuboid other);

}
