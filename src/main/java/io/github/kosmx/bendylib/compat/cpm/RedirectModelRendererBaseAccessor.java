package io.github.kosmx.bendylib.compat.cpm;

import com.tom.cpm.shared.model.render.VanillaModelPart;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.tom.cpm.client.PlayerRenderManager$RedirectModelRendererBase", remap = false)
public interface RedirectModelRendererBaseAccessor {

    @Accessor("parent")
    ModelPart bendylib$getParent();

    @Accessor("part")
    VanillaModelPart bendylib$getPart();
}
