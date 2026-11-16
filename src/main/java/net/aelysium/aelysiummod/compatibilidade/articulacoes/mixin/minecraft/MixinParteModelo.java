package net.aelysium.aelysiummod.compatibilidade.articulacoes.mixin.minecraft;

import net.minecraft.client.model.geom.ModelPart;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.acesso.AcessoParteModelo;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.cuboide.CuboideMutavel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

@Mixin(ModelPart.class)
public abstract class MixinParteModelo implements AcessoParteModelo {

    @Mutable
    @Shadow
    @Final
    private List<ModelPart.Cube> cubes;

    @Override
    public List<ModelPart.Cube> cpmec$obterCubos() {
        return cubes;
    }

    @Override
    public void cpmec$definirCubos(List<ModelPart.Cube> cubos) {
        this.cubes = cubos;
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void cpmec$copiarEstadoDosCubos(ModelPart origem, CallbackInfo ci) {
        List<ModelPart.Cube> cubosOrigem = ((AcessoParteModelo) (Object) origem).cpmec$obterCubos();
        if (cubosOrigem == null || cubes == null) {
            return;
        }
        Iterator<ModelPart.Cube> iteradorOrigem = cubosOrigem.iterator();
        Iterator<ModelPart.Cube> iteradorDestino = cubes.iterator();
        while (iteradorOrigem.hasNext() && iteradorDestino.hasNext()) {
            ((CuboideMutavel) (Object) iteradorDestino.next()).cpmec$copiarEstadoDe((CuboideMutavel) (Object) iteradorOrigem.next());
        }
    }
}
