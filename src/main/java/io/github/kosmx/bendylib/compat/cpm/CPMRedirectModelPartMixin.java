package io.github.kosmx.bendylib.compat.cpm;

import io.github.kosmx.bendylib.impl.accessors.IModelPartAccessor;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.tom.cpm.client.PlayerRenderManager$RedirectModelRendererBase", remap = false)
public abstract class CPMRedirectModelPartMixin {

    @Shadow
    protected ModelPart parent;

    @Inject(method = "swapIn()Lnet/minecraft/client/model/geom/ModelPart;", at = @At("RETURN"), require = 0)
    private void bendylib$relinkCubes(CallbackInfoReturnable<ModelPart> cir) {
        if (parent != null) {
            ((IModelPartAccessor) cir.getReturnValue()).setCubes(((IModelPartAccessor) parent).getCubes());
        }
    }
}
