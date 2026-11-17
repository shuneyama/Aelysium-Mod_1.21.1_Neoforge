package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

public class ModeloPorcoFilhote<T extends Entity> extends PigModel<T> {

    private final ModeloQuadrupedeFilhote q;

    public ModeloPorcoFilhote(ModelPart raiz) {
        super(raiz);
        this.q = new ModeloQuadrupedeFilhote(raiz);
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.0F, -4.5F, 7.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, 0.5F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 15).addBox(-3.5F, -5.0F, -5.0F, 7.0F, 6.0F, 6.0F, new CubeDeformation(0.025F)).texOffs(6, 27).addBox(-1.5F, -1.975F, -6.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.015F)), PartPose.offset(0.0F, 19.0F, -2.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 22.0F, -3.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(23, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 22.0F, -3.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 22.0F, 4.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(23, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 22.0F, 4.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(T entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        q.animar(limbSwing, limbSwingAmount, netHeadYaw, headPitch);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int luz, int sobreposicao, int cor) {
        q.renderizar(poseStack, buffer, luz, sobreposicao, cor);
    }
}
