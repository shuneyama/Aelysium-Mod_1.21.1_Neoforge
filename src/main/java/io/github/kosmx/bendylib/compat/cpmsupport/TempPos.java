package io.github.kosmx.bendylib.compat.cpmsupport;

import io.github.kosmx.bendylib.impl.IPosWithOrigin;
import org.joml.Vector3f;

public final class TempPos implements IPosWithOrigin {
    private final Vector3f original;
    private Vector3f result;

    public TempPos(Vector3f original) {
        this.original = original;
    }

    @Override
    public Vector3f getOriginalPos() {
        return new Vector3f(original);
    }

    @Override
    public Vector3f getPos() {
        return result != null ? result : original;
    }

    @Override
    public void setPos(Vector3f vector3f) {
        this.result = vector3f;
    }
}
