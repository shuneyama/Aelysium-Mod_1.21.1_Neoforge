package io.github.kosmx.bendylib.impl;

import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;

public interface IVertex {

    Vector3f getPos();

    float getU();

    float getV();

    IVertex remap(float u, float v);

    default ModelPart.Vertex toMojVertex(){
        return new ModelPart.Vertex(this.getPos(), getU(), getV());
    }

}
