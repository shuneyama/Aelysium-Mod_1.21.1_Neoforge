package io.github.kosmx.bendylib.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;

import java.util.List;

public interface ICuboid {

    void render(PoseStack.Pose matrices, VertexConsumer vertexConsumer, int light, int overlay, int color);

    void copyState(ICuboid other);

    default boolean disableAfterDraw(){
        return true;
    }

    default List<ModelPart.Polygon> getQuads(){
        return null;
    }
}
