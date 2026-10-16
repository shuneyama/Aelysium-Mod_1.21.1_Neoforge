package io.github.kosmx.bendylib.compat.cpm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.kosmx.bendylib.ModelPartAccessor;
import io.github.kosmx.bendylib.compat.cpmsupport.BendContext;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.tom.cpm.client.PlayerRenderManager$RedirectModelRendererVanilla", remap = false)
public abstract class CPMBendContextMixin {

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"), require = 0)
    private void bendylib$setBendContext(PoseStack matrixStackIn, VertexConsumer bufferIn, int light, int overlay, int color, CallbackInfo ci) {
        BendContext.clear();
        if (!net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda.isAutorizadoAgora()) {
            return;
        }
        ModelPart parent = ((RedirectModelRendererBaseAccessor) (Object) this).bendylib$getParent();
        if (parent != null) {
            ModelPartAccessor.optionalGetCuboid(parent, 0).ifPresent(mutableCuboid -> {
                if (mutableCuboid.getActiveMutator() != null && mutableCuboid.getActiveMutator().getSecond() instanceof BendableCuboid bendable) {
                    BendContext.set(bendable);
                }
            });
        }
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("RETURN"), require = 0)
    private void bendylib$clearBendContext(PoseStack matrixStackIn, VertexConsumer bufferIn, int light, int overlay, int color, CallbackInfo ci) {
        BendContext.clear();
    }
}
