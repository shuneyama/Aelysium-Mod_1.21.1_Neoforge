package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class ModeloGalinhaFilhote<T extends Entity> extends ChickenModel<T> {

    private final ModelPart raiz;
    private final ModelPart pernaDireita;
    private final ModelPart pernaEsquerda;
    private final ModelPart asaDireita;
    private final ModelPart asaEsquerda;

    public ModeloGalinhaFilhote(ModelPart raiz) {
        super(raiz);
        this.raiz = raiz;
        this.pernaDireita = raiz.getChild("right_leg");
        this.pernaEsquerda = raiz.getChild("left_leg");
        this.asaDireita = raiz.getChild("right_wing");
        this.asaEsquerda = raiz.getChild("left_wing");
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.25F, -0.75F, 4.0F, 4.0F, 4.0F).texOffs(10, 8).addBox(-1.0F, -0.25F, -1.75F, 2.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 20.25F, -1.25F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(2, 2).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0F).texOffs(0, 1).addBox(-0.5F, 2.0F, -1.0F, 1.0F, 0.0F, 1.0F), PartPose.offset(1.0F, 22.0F, 0.5F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 2).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0F).texOffs(0, 0).addBox(-0.5F, 2.0F, -1.0F, 1.0F, 0.0F, 1.0F), PartPose.offset(-1.0F, 22.0F, 0.5F));
        root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(6, 8).addBox(0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F), PartPose.offset(2.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(4, 8).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F), PartPose.offset(-2.0F, 20.0F, 0.0F));
        UtilFilhotes.vazias(root, "head", "beak", "red_thing");
        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void setupAnim(T entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        UtilFilhotes.resetar(raiz);
        float posicao = UtilFilhotes.posicaoAndar(limbSwing);
        pernaDireita.xRot = Mth.cos(posicao * 0.6662F) * 1.4F * limbSwingAmount;
        pernaEsquerda.xRot = Mth.cos(posicao * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        asaDireita.zRot = ageInTicks;
        asaEsquerda.zRot = -ageInTicks;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int luz, int sobreposicao, int cor) {
        raiz.render(poseStack, buffer, luz, sobreposicao, cor);
    }
}
