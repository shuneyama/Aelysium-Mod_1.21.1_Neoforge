package net.aelysium.aelysiummod.compatibilidade.articulacoes.mixin.cpm;

import com.tom.cpl.math.MatrixStack;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.cpm.ContextoDobra;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.cpm.ConversaoMatriz;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.tom.cpm.shared.model.render.ModelRenderManager$RedirectRenderer", remap = false)
public interface MixinRenderizadorRedirecionado {

    @Inject(method = "translateRotate(Lcom/tom/cpl/math/MatrixStack;)V", at = @At("TAIL"), require = 0)
    private void cpmec$capturarReferencia(MatrixStack pilha, CallbackInfo ci) {
        if (ContextoDobra.obter() != null) {
            ContextoDobra.definirReferencia(ConversaoMatriz.paraJoml(pilha.getLast().getMatrix()));
        }
    }
}
