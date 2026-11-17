package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.minecraft.client.model.TurtleModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Turtle;

public class ModeloTartarugaFilhote extends TurtleModel<Turtle> {

    private final ModeloQuadrupedeFilhote q;

    public ModeloTartarugaFilhote(ModelPart raiz) {
        super(raiz);
        this.q = new ModeloQuadrupedeFilhote(raiz);
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F), PartPose.offset(0.0F, 22.9F, 1.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, -2.0F, -3.0F, 3.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 22.9F, -1.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(-1, 0).addBox(-2.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F), PartPose.offset(-2.0F, 23.9F, 2.5F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(-1, 1).addBox(0.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F), PartPose.offset(2.0F, 23.9F, 2.5F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(8, 6).addBox(-2.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F), PartPose.offset(-2.0F, 23.9F, -0.5F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(8, 7).addBox(0.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F), PartPose.offset(2.0F, 23.9F, -0.5F));
        UtilFilhotes.vazias(root, "egg_belly");
        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void setupAnim(Turtle entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        q.animar(limbSwing, limbSwingAmount, netHeadYaw, headPitch);
        float posicao = UtilFilhotes.posicaoAndar(limbSwing);
        if (!entidade.isInWater() && entidade.onGround()) {
            float botar = entidade.isLayingEgg() ? 4.0F : 1.0F;
            float amplitude = entidade.isLayingEgg() ? 2.0F : 1.0F;
            float balanco = posicao * 5.0F;
            float frente = Mth.cos(botar * balanco);
            float tras = Mth.cos(balanco);
            q.dianteiraDireita.yRot = -frente * 8.0F * limbSwingAmount * amplitude;
            q.dianteiraEsquerda.yRot = frente * 8.0F * limbSwingAmount * amplitude;
            q.traseiraDireita.yRot = -tras * 3.0F * limbSwingAmount;
            q.traseiraEsquerda.yRot = tras * 3.0F * limbSwingAmount;
        } else {
            float escala = 0.5F * limbSwingAmount;
            float balanco = Mth.cos(posicao * 0.6662F * 0.6F) * escala;
            q.traseiraDireita.xRot = balanco;
            q.traseiraEsquerda.xRot = -balanco;
            q.dianteiraDireita.zRot = -balanco;
            q.dianteiraEsquerda.zRot = balanco;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int luz, int sobreposicao, int cor) {
        q.renderizar(poseStack, buffer, luz, sobreposicao, cor);
    }
}
