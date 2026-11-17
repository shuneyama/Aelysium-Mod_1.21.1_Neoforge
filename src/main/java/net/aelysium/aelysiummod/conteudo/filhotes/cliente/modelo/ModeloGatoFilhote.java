package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Cat;

public class ModeloGatoFilhote extends CatModel<Cat> {

    private final AnimacaoFelinoFilhote animacao;
    private float deitar;
    private float deitarCauda;
    private float relaxar;

    public ModeloGatoFilhote(ModelPart raiz) {
        super(raiz);
        this.animacao = new AnimacaoFelinoFilhote(raiz);
    }

    @Override
    public void prepareMobModel(Cat entidade, float limbSwing, float limbSwingAmount, float partialTick) {
        this.deitar = entidade.getLieDownAmount(partialTick);
        this.deitarCauda = entidade.getLieDownAmountTail(partialTick);
        this.relaxar = entidade.getRelaxStateOneAmount(partialTick);
    }

    @Override
    public void setupAnim(Cat entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        animacao.animar(entidade.isCrouching(), entidade.isSprinting(), entidade.isInSittingPose(), deitar, deitarCauda, relaxar, entidade.getAgeScale(), limbSwing, limbSwingAmount, netHeadYaw, headPitch);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int luz, int sobreposicao, int cor) {
        animacao.raiz.render(poseStack, buffer, luz, sobreposicao, cor);
    }
}
