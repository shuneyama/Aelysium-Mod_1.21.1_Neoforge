package net.aelysium.aelysiummod.compatibilidade.articulacoes.mixin.cpm;

import net.minecraft.client.model.geom.ModelPart;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.acesso.AcessoParteModelo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.tom.cpm.client.PlayerRenderManager$RedirectModelRendererBase", remap = false)
public abstract class MixinRedirecionadorBase {

    @Shadow
    protected ModelPart parent;

    @Inject(method = "swapIn()Lnet/minecraft/client/model/geom/ModelPart;", at = @At("RETURN"), require = 0)
    private void cpmec$religarCubos(CallbackInfoReturnable<ModelPart> cir) {
        ModelPart redirecionada = cir.getReturnValue();
        if (parent != null && redirecionada != null) {
            ((AcessoParteModelo) (Object) redirecionada).cpmec$definirCubos(((AcessoParteModelo) (Object) parent).cpmec$obterCubos());
        }
    }
}
