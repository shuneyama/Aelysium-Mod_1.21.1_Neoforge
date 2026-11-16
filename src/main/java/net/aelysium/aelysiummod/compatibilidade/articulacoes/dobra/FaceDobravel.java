package net.aelysium.aelysiummod.compatibilidade.articulacoes.dobra;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class FaceDobravel {

    private record Vertice(float u, float v, PosicaoMemorizada posicao) {
    }

    private final Vertice[] vertices = new Vertice[4];

    public FaceDobravel(PosicaoMemorizada[] posicoes, float u1, float v1, float u2, float v2, float larguraTextura, float alturaTextura, boolean espelhado) {
        vertices[0] = new Vertice(u2 / larguraTextura, v1 / alturaTextura, posicoes[0]);
        vertices[1] = new Vertice(u1 / larguraTextura, v1 / alturaTextura, posicoes[1]);
        vertices[2] = new Vertice(u1 / larguraTextura, v2 / alturaTextura, posicoes[2]);
        vertices[3] = new Vertice(u2 / larguraTextura, v2 / alturaTextura, posicoes[3]);
        if (espelhado) {
            Vertice temporario = vertices[0];
            vertices[0] = vertices[3];
            vertices[3] = temporario;
            temporario = vertices[1];
            vertices[1] = vertices[2];
            vertices[2] = temporario;
        }
    }

    public void renderizar(PoseStack.Pose pose, VertexConsumer consumidor, int luz, int sobreposicao, int cor) {
        Vector3f[] posicoes = new Vector3f[4];
        for (int i = 0; i < 4; i++) {
            posicoes[i] = vertices[i].posicao().posicao();
        }
        Vector3f normal = pose.transformNormal(calcularNormal(posicoes), new Vector3f());
        Matrix4f matrizPosicao = pose.pose();
        Vector3f transformada = new Vector3f();
        for (int i = 0; i < 4; i++) {
            Vector3f posicao = posicoes[i];
            matrizPosicao.transformPosition(posicao.x() / 16f, posicao.y() / 16f, posicao.z() / 16f, transformada);
            consumidor.addVertex(transformada.x(), transformada.y(), transformada.z(), cor, vertices[i].u(), vertices[i].v(), sobreposicao, luz, normal.x(), normal.y(), normal.z());
        }
    }

    public static Vector3f calcularNormal(Vector3f[] cantos) {
        Vector3f vetorB = new Vector3f(cantos[1]).sub(cantos[3]);
        Vector3f vetorA = new Vector3f(cantos[0]).sub(cantos[2]);
        vetorA.cross(vetorB);
        if (vetorA.lengthSquared() < 1.0E-8f) {
            return Direction.NORTH.step();
        }
        return vetorA.normalize();
    }
}
