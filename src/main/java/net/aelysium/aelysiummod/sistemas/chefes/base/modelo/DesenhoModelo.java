package net.aelysium.aelysiummod.sistemas.chefes.base.modelo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class DesenhoModelo {
    private static final Quaternionf ROTACAO = new Quaternionf();
    private static final Vector3f POSICAO = new Vector3f();
    private static final Vector3f NORMAL = new Vector3f();

    private DesenhoModelo() {
    }

    public interface AjusteOsso {
        int luz(OssoModelo osso, int luz);

        int cor(OssoModelo osso, int cor);
    }

    public static void desenhar(PoseStack pilha, ModeloBedrock modelo, VertexConsumer consumidor, int luz, int sobreposicao, int cor, AjusteOsso ajuste) {
        for (OssoModelo osso : modelo.raizes()) {
            osso(pilha, osso, consumidor, luz, sobreposicao, cor, ajuste);
        }
    }

    private static void osso(PoseStack pilha, OssoModelo osso, VertexConsumer consumidor, int luz, int sobreposicao, int cor, AjusteOsso ajuste) {
        if (ajuste != null) {
            luz = ajuste.luz(osso, luz);
            cor = ajuste.cor(osso, cor);
        }

        pilha.pushPose();
        pilha.translate(-osso.getPosX() / 16f, osso.getPosY() / 16f, osso.getPosZ() / 16f);
        pilha.translate(osso.getPivoX() / 16f, osso.getPivoY() / 16f, osso.getPivoZ() / 16f);
        float rx = osso.getRotX();
        float ry = osso.getRotY();
        float rz = osso.getRotZ();
        if (rx != 0 || ry != 0 || rz != 0) pilha.mulPose(ROTACAO.rotationZYX(rz, ry, rx));
        pilha.scale(osso.getEscalaX(), osso.getEscalaY(), osso.getEscalaZ());
        pilha.translate(-osso.getPivoX() / 16f, -osso.getPivoY() / 16f, -osso.getPivoZ() / 16f);

        if (!osso.isOculto()) {
            for (CuboModelo cubo : osso.getCubos()) {
                pilha.pushPose();
                cubo(pilha, cubo, consumidor, luz, sobreposicao, cor);
                pilha.popPose();
            }
        }

        for (OssoModelo filho : osso.getFilhos()) {
            osso(pilha, filho, consumidor, luz, sobreposicao, cor, ajuste);
        }
        pilha.popPose();
    }

    private static void cubo(PoseStack pilha, CuboModelo cubo, VertexConsumer consumidor, int luz, int sobreposicao, int cor) {
        if (cubo.temRotacao()) {
            pilha.translate(cubo.pivoX() / 16f, cubo.pivoY() / 16f, cubo.pivoZ() / 16f);
            if (cubo.rotZ() != 0) pilha.mulPose(ROTACAO.rotationZ(cubo.rotZ()));
            if (cubo.rotY() != 0) pilha.mulPose(ROTACAO.rotationY(cubo.rotY()));
            if (cubo.rotX() != 0) pilha.mulPose(ROTACAO.rotationX(cubo.rotX()));
            pilha.translate(-cubo.pivoX() / 16f, -cubo.pivoY() / 16f, -cubo.pivoZ() / 16f);
        }

        Matrix3f normais = pilha.last().normal();
        Matrix4f pose = pilha.last().pose();

        for (QuadModelo quad : cubo.quads()) {
            if (quad == null) continue;

            normais.transform(quad.normalX(), quad.normalY(), quad.normalZ(), NORMAL);
            if (NORMAL.x() < 0 && (cubo.altura() == 0 || cubo.profundidade() == 0)) NORMAL.mul(-1, 1, 1);
            if (NORMAL.y() < 0 && (cubo.largura() == 0 || cubo.profundidade() == 0)) NORMAL.mul(1, -1, 1);
            if (NORMAL.z() < 0 && (cubo.largura() == 0 || cubo.altura() == 0)) NORMAL.mul(1, 1, -1);

            for (VerticeModelo vertice : quad.vertices()) {
                pose.transformPosition(vertice.x(), vertice.y(), vertice.z(), POSICAO);
                consumidor.addVertex(POSICAO.x(), POSICAO.y(), POSICAO.z(), cor, vertice.u(), vertice.v(), sobreposicao, luz, NORMAL.x(), NORMAL.y(), NORMAL.z());
            }
        }
    }
}
