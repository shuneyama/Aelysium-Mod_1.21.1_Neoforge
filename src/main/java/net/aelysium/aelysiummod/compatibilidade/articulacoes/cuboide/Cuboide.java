package net.aelysium.aelysiummod.compatibilidade.articulacoes.cuboide;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public interface Cuboide {

    void renderizar(PoseStack.Pose pose, VertexConsumer consumidor, int luz, int sobreposicao, int cor);

    void copiarEstado(Cuboide outro);
}
