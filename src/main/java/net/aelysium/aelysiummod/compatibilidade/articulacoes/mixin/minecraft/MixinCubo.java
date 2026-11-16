package net.aelysium.aelysiummod.compatibilidade.articulacoes.mixin.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.cuboide.Cuboide;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.cuboide.CuboideMutavel;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.cuboide.DadosCuboide;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Mixin(ModelPart.Cube.class)
public abstract class MixinCubo implements CuboideMutavel {

    @Shadow
    @Final
    public float minX;

    @Shadow
    @Final
    public float minY;

    @Shadow
    @Final
    public float minZ;

    @Unique
    private Map<String, Cuboide> cpmec$mutadores = new HashMap<>();

    @Unique
    private Map<String, Function<DadosCuboide, Cuboide>> cpmec$construtores = new HashMap<>();

    @Unique
    private DadosCuboide cpmec$dados;

    @Unique
    @Nullable
    private Cuboide cpmec$ativo;

    @Unique
    @Nullable
    private String cpmec$idAtivo;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cpmec$guardarDados(int u, int v, float x, float y, float z, float tamanhoX, float tamanhoY, float tamanhoZ, float extraX, float extraY, float extraZ, boolean espelhado, float larguraTextura, float alturaTextura, Set<Direction> faces, CallbackInfo ci) {
        cpmec$dados = new DadosCuboide(u, v, minX, minY, minZ, tamanhoX, tamanhoY, tamanhoZ, extraX, extraY, extraZ, espelhado, larguraTextura, alturaTextura);
    }

    @Override
    public boolean cpmec$registrarMutador(String nome, Function<DadosCuboide, Cuboide> construtor) {
        if (cpmec$construtores.containsKey(nome)) {
            return false;
        }
        cpmec$construtores.put(nome, construtor);
        return true;
    }

    @Nullable
    @Override
    public Cuboide cpmec$ativarMutador(@Nullable String nome) {
        if (nome == null) {
            cpmec$ativo = null;
            cpmec$idAtivo = null;
            return null;
        }
        Function<DadosCuboide, Cuboide> construtor = cpmec$construtores.get(nome);
        if (construtor == null) {
            return null;
        }
        cpmec$idAtivo = nome;
        return cpmec$ativo = cpmec$mutadores.computeIfAbsent(nome, chave -> construtor.apply(cpmec$dados));
    }

    @Nullable
    @Override
    public String cpmec$idMutadorAtivo() {
        return cpmec$idAtivo;
    }

    @Nullable
    @Override
    public Cuboide cpmec$mutadorAtivo() {
        return cpmec$ativo;
    }

    @Override
    public void cpmec$copiarEstadoDe(CuboideMutavel outro) {
        Cuboide ativoOutro = outro.cpmec$mutadorAtivo();
        if (ativoOutro == null) {
            cpmec$ativarMutador(null);
            return;
        }
        Cuboide ativo = cpmec$ativarMutador(outro.cpmec$idMutadorAtivo());
        if (ativo != null) {
            ativo.copiarEstado(ativoOutro);
        }
    }

    @Inject(method = "compile", at = @At("HEAD"), cancellable = true)
    private void cpmec$renderizarMutador(PoseStack.Pose pose, VertexConsumer consumidor, int luz, int sobreposicao, int cor, CallbackInfo ci) {
        if (cpmec$ativo != null) {
            cpmec$ativo.renderizar(pose, consumidor, luz, sobreposicao, cor);
            ci.cancel();
        }
    }
}
