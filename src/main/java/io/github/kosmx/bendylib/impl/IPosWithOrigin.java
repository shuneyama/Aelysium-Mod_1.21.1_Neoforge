package io.github.kosmx.bendylib.impl;

import org.joml.Vector3f;

public interface IPosWithOrigin {
    Vector3f getOriginalPos();

    Vector3f getPos();

    void setPos(Vector3f vector3f);
}
