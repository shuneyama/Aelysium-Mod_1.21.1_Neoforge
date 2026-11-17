package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Ocelot;

public class ModeloJaguatiricaFilhote extends OcelotModel<Ocelot> {

    private final AnimacaoFelinoFilhote animacao;

    public ModeloJaguatiricaFilhote(ModelPart raiz) {
        super(raiz);
        this.animacao = new AnimacaoFelinoFilhote(raiz);
    }

    @Override
    public void prepareMobModel(Ocelot entidade, float limbSwing, float limbSwingAmount, float partialTick) {
    }

    @Override
    public void setupAnim(Ocelot entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        animacao.animar(entidade.isCrouching(), entidade.isSprinting(), false, 0.0F, 0.0F, 0.0F, entidade.getAgeScale(), limbSwing, limbSwingAmount, netHeadYaw, headPitch);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int luz, int sobreposicao, int cor) {
        animacao.raiz.render(poseStack, buffer, luz, sobreposicao, cor);
    }
}
