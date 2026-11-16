package net.aelysium.aelysiummod.compatibilidade.articulacoes.mixin.cpm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.acesso.AcessoParteModelo;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.cpm.ContextoDobra;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.dobra.CuboideDobravel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.tom.cpm.client.PlayerRenderManager$RedirectModelRendererVanilla", remap = false)
public abstract class MixinRedirecionadorVanilla {

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"), require = 0)
    private void cpmec$definirContextoDobra(PoseStack pilha, VertexConsumer consumidor, int luz, int sobreposicao, int cor, CallbackInfo ci) {
        ContextoDobra.limpar();
        AcessoParteModelo.primeiroCuboide(((AcessorRedirecionadorBase) (Object) this).cpmec$obterPai()).ifPresent(cuboide -> {
            if (cuboide.cpmec$mutadorAtivo() instanceof CuboideDobravel dobravel) {
                ContextoDobra.definir(dobravel);
            }
        });
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("RETURN"), require = 0)
    private void cpmec$limparContextoDobra(PoseStack pilha, VertexConsumer consumidor, int luz, int sobreposicao, int cor, CallbackInfo ci) {
        ContextoDobra.limpar();
    }
}
