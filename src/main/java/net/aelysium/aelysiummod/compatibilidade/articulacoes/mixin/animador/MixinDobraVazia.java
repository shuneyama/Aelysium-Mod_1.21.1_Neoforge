package net.aelysium.aelysiummod.compatibilidade.articulacoes.mixin.animador;

import dev.kosmx.playerAnim.core.util.Pair;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.animador.DobraAnimador;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.kosmx.playerAnim.impl.animation.IBendHelper$DummyBendable", remap = false)
public abstract class MixinDobraVazia {

    @Inject(method = "bend(Lnet/minecraft/client/model/geom/ModelPart;FF)V", at = @At("HEAD"), require = 1)
    private void cpmec$dobrar(ModelPart parte, float eixo, float valor, CallbackInfo ci) {
        DobraAnimador.dobrar(parte, eixo, valor);
    }

    @Inject(method = "bend(Lnet/minecraft/client/model/geom/ModelPart;Ldev/kosmx/playerAnim/core/util/Pair;)V", at = @At("HEAD"), require = 1)
    private void cpmec$dobrarPar(ModelPart parte, Pair<Float, Float> par, CallbackInfo ci) {
        if (par == null) {
            DobraAnimador.desativar(parte);
        } else {
            DobraAnimador.dobrar(parte, par.getLeft(), par.getRight());
        }
    }

    @Inject(method = "initBend", at = @At("HEAD"), require = 1)
    private void cpmec$prepararDobra(ModelPart parte, Direction direcao, CallbackInfo ci) {
        DobraAnimador.prepararDobra(parte, direcao);
    }

    @Inject(method = "initCapeBend", at = @At("HEAD"), require = 1)
    private void cpmec$prepararDobraCapa(ModelPart parte, CallbackInfo ci) {
        DobraAnimador.prepararDobraCapa(parte);
    }
}
