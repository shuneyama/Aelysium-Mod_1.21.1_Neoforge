package net.aelysium.aelysiummod.compatibilidade.articulacoes.mixin.cpm;

import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.render.VertexBuffer;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.cpm.ContextoDobra;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.cpm.ConversaoMatriz;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.cpm.ReflexaoCpm;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.cpm.SubFace;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.dobra.CuboideDobravel;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.dobra.FaceDobravel;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.dobra.PosicaoTemporaria;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "com.tom.cpm.shared.model.render.BoxRender$QuadMesh", remap = false)
public abstract class MixinMalhaQuads {

    @Unique
    private static final int CPMEC$MAXIMO_SEGMENTOS = 24;

    @Unique
    private static final float CPMEC$PIXEL = 16f;

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true, require = 0)
    private void cpmec$desenharDobrado(MatrixStack pilha, VertexBuffer buffer, float vermelho, float verde, float azul, float alfa, CallbackInfo ci) {
        CuboideDobravel dobravel = ContextoDobra.obter();
        if (dobravel == null) {
            return;
        }
        Object[] quads = ReflexaoCpm.obterQuads(this);
        if (quads == null || quads.length == 0) {
            return;
        }
        ci.cancel();

        Matrix4f matrizCubo = ConversaoMatriz.paraJoml(pilha.getLast().getMatrix());
        Matrix4f referencia = ContextoDobra.obterReferencia();
        Matrix4f matrizSaida = referencia != null ? referencia : matrizCubo;
        Matrix4f cuboParaReferencia = referencia != null ? new Matrix4f(referencia).invert().mul(matrizCubo) : new Matrix4f();
        Matrix3f matrizNormal = matrizSaida.normal(new Matrix3f());
        Vector3f direcaoDobra = dobravel.direcaoDobra().step();

        List<SubFace> subFaces = new ArrayList<>();
        List<PosicaoTemporaria> pontos = new ArrayList<>();
        for (Object quad : quads) {
            Object[] vertices = ReflexaoCpm.obterVertices(quad);
            if (vertices == null || vertices.length != 4) {
                continue;
            }
            Vector3f[] cantos = new Vector3f[4];
            float[] us = new float[4];
            float[] vs = new float[4];
            for (int i = 0; i < 4; i++) {
                AcessorVerticeCpm vertice = (AcessorVerticeCpm) vertices[i];
                Vec3f posicao = vertice.cpmec$obterPosicao();
                cantos[i] = cuboParaReferencia.transformPosition(new Vector3f(posicao.x, posicao.y, posicao.z).div(CPMEC$PIXEL)).mul(CPMEC$PIXEL);
                us[i] = vertice.cpmec$obterTexturaU();
                vs[i] = vertice.cpmec$obterTexturaV();
            }
            cpmec$subdividir(cantos, us, vs, direcaoDobra, subFaces, pontos);
        }

        dobravel.aplicarDobra(dobravel.obterEixoDobra(), dobravel.obterDobra(), consumidor -> pontos.forEach(consumidor));

        Vector3f resolvido = new Vector3f();
        for (SubFace subFace : subFaces) {
            Vector3f[] dobrados = new Vector3f[4];
            for (int i = 0; i < 4; i++) {
                dobrados[i] = subFace.pontos()[i].posicao();
            }

            Vector3f normal = matrizNormal.transform(FaceDobravel.calcularNormal(dobrados)).normalize();

            for (int i = 0; i < 4; i++) {
                matrizSaida.transformPosition(resolvido.set(dobrados[i]).div(CPMEC$PIXEL));
                buffer.pos(resolvido.x, resolvido.y, resolvido.z)
                        .color(vermelho, verde, azul, alfa)
                        .tex(subFace.u()[i], subFace.v()[i])
                        .normal(normal.x, normal.y, normal.z)
                        .endVertex();
            }
        }
    }

    @Unique
    private static void cpmec$subdividir(Vector3f[] cantos, float[] u, float[] v, Vector3f direcaoDobra, List<SubFace> subFaces, List<PosicaoTemporaria> pontos) {
        float extensaoS = Math.abs(new Vector3f(cantos[1]).sub(cantos[0]).dot(direcaoDobra));
        float extensaoT = Math.abs(new Vector3f(cantos[3]).sub(cantos[0]).dot(direcaoDobra));
        boolean dividirEmS = extensaoS >= extensaoT;
        float extensao = dividirEmS ? extensaoS : extensaoT;
        int segmentos = Math.max(1, Math.min(CPMEC$MAXIMO_SEGMENTOS, Math.round(extensao)));

        PosicaoTemporaria[] linha0 = new PosicaoTemporaria[segmentos + 1];
        PosicaoTemporaria[] linha1 = new PosicaoTemporaria[segmentos + 1];
        float[] linha0u = new float[segmentos + 1];
        float[] linha0v = new float[segmentos + 1];
        float[] linha1u = new float[segmentos + 1];
        float[] linha1v = new float[segmentos + 1];

        int a0 = 0;
        int a1 = dividirEmS ? 1 : 3;
        int b0 = dividirEmS ? 3 : 1;
        int b1 = 2;

        for (int i = 0; i <= segmentos; i++) {
            float t = (float) i / segmentos;
            linha0[i] = new PosicaoTemporaria(new Vector3f(cantos[a0]).lerp(cantos[a1], t));
            linha1[i] = new PosicaoTemporaria(new Vector3f(cantos[b0]).lerp(cantos[b1], t));
            linha0u[i] = cpmec$interpolar(u[a0], u[a1], t);
            linha0v[i] = cpmec$interpolar(v[a0], v[a1], t);
            linha1u[i] = cpmec$interpolar(u[b0], u[b1], t);
            linha1v[i] = cpmec$interpolar(v[b0], v[b1], t);
            pontos.add(linha0[i]);
            pontos.add(linha1[i]);
        }

        for (int i = 0; i < segmentos; i++) {
            if (dividirEmS) {
                subFaces.add(new SubFace(
                        new PosicaoTemporaria[]{linha0[i], linha0[i + 1], linha1[i + 1], linha1[i]},
                        new float[]{linha0u[i], linha0u[i + 1], linha1u[i + 1], linha1u[i]},
                        new float[]{linha0v[i], linha0v[i + 1], linha1v[i + 1], linha1v[i]}));
            } else {
                subFaces.add(new SubFace(
                        new PosicaoTemporaria[]{linha0[i], linha1[i], linha1[i + 1], linha0[i + 1]},
                        new float[]{linha0u[i], linha1u[i], linha1u[i + 1], linha0u[i + 1]},
                        new float[]{linha0v[i], linha1v[i], linha1v[i + 1], linha0v[i + 1]}));
            }
        }
    }

    @Unique
    private static float cpmec$interpolar(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
