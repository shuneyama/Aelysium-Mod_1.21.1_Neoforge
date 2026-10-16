package io.github.kosmx.bendylib.compat.cpm;

import com.tom.cpl.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.tom.cpm.shared.model.render.BoxRender$Quad", remap = false)
public interface QuadAccessor {

    @Accessor("normal")
    Vec3f bendylib$getNormal();
}
