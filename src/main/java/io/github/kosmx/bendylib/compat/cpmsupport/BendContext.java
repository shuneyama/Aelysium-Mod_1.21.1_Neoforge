package io.github.kosmx.bendylib.compat.cpmsupport;

import io.github.kosmx.bendylib.impl.BendableCuboid;

public final class BendContext {

    private static BendableCuboid current;

    public static void set(BendableCuboid cuboid) {
        current = cuboid;
    }

    public static void clear() {
        current = null;
    }

    public static BendableCuboid get() {
        return current;
    }

    private BendContext() {
    }
}
