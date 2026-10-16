package io.github.kosmx.bendylib;

import io.github.kosmx.bendylib.impl.DummyCuboid;
import io.github.kosmx.bendylib.impl.accessors.IModelPartAccessor;
import net.minecraft.client.model.geom.ModelPart;

import java.util.*;

public final class ModelPartAccessor {

    public static Map<String,ModelPart> getChildren(ModelPart modelPart){
        return ((IModelPartAccessor)modelPart).getChildren();
    }

    @Deprecated
    public static MutableCuboid getCuboid(ModelPart modelPart, int index){
        Optional<MutableCuboid> optionalMutableCuboid = optionalGetCuboid(modelPart, index);
        return optionalMutableCuboid.orElseGet(DummyCuboid::new);
    }

    public static Optional<MutableCuboid> optionalGetCuboid(ModelPart modelPart, int index){
        if(modelPart == null || getCuboids(modelPart) == null || getCuboids(modelPart).size() <= index) return Optional.empty();
        return Optional.of((MutableCuboid)getCuboids(modelPart).get(index));
    }

    public static List<ModelPart.Cube> getCuboids(ModelPart modelPart){
        return ((IModelPartAccessor)modelPart).getCubes();
    }

    public enum Workaround {
        ExportQuads, VanillaDraw, None;
    }
}
