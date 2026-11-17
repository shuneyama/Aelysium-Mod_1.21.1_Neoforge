package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.minecraft.client.model.geom.ModelPart;

public final class ModeloQuadrupedeFilhote {

    public final ModelPart raiz;
    public final ModelPart cabeca;
    public final ModelPart corpo;
    public final ModelPart traseiraDireita;
    public final ModelPart traseiraEsquerda;
    public final ModelPart dianteiraDireita;
    public final ModelPart dianteiraEsquerda;

    public ModeloQuadrupedeFilhote(ModelPart raiz) {
        this.raiz = raiz;
        this.cabeca = raiz.getChild("head");
        this.corpo = raiz.getChild("body");
        this.traseiraDireita = raiz.getChild("right_hind_leg");
        this.traseiraEsquerda = raiz.getChild("left_hind_leg");
        this.dianteiraDireita = raiz.getChild("right_front_leg");
        this.dianteiraEsquerda = raiz.getChild("left_front_leg");
    }

    public void animar(float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch) {
        UtilFilhotes.resetar(raiz);
        UtilFilhotes.cabeca(cabeca, headPitch, netHeadYaw);
        UtilFilhotes.pernas(traseiraDireita, traseiraEsquerda, dianteiraDireita, dianteiraEsquerda, UtilFilhotes.posicaoAndar(limbSwing), limbSwingAmount);
    }

    public void renderizar(PoseStack poseStack, VertexConsumer buffer, int luz, int sobreposicao, int cor) {
        raiz.render(poseStack, buffer, luz, sobreposicao, cor);
    }
}
