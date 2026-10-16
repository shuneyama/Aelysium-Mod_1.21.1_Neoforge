package io.github.kosmx.bendylib.impl.accessors;

import io.github.kosmx.bendylib.ModelPartAccessor;
import net.minecraft.client.model.geom.ModelPart;

import java.util.List;
import java.util.Map;

public interface IModelPartAccessor {

    List<ModelPart.Cube> getCubes();

    void setCubes(List<ModelPart.Cube> cubes);

    Map<String, ModelPart> getChildren();

    void setWorkaround(ModelPartAccessor.Workaround workaround);

}
