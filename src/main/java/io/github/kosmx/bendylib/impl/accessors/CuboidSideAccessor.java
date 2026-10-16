package io.github.kosmx.bendylib.impl.accessors;

import net.minecraft.client.model.geom.ModelPart;

public interface CuboidSideAccessor {
    ModelPart.Polygon[] getSides();

    void setSides(ModelPart.Polygon[] sides);

    void resetSides();

    void doSideSwapping();
}
