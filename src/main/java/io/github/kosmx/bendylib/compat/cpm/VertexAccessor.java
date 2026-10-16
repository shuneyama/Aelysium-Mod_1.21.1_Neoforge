package io.github.kosmx.bendylib.compat.cpm;

import com.tom.cpl.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.tom.cpm.shared.model.render.BoxRender$Vertex", remap = false)
public interface VertexAccessor {

    @Accessor("position")
    Vec3f bendylib$getPosition();

    @Accessor("textureU")
    float bendylib$getTextureU();

    @Accessor("textureV")
    float bendylib$getTextureV();
}
