package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ModelUtils;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Panda;

public class ModeloPandaFilhote extends PandaModel<Panda> {

    private final ModeloQuadrupedeFilhote q;
    private float sentar;
    private float deitarCostas;

    public ModeloPandaFilhote(ModelPart raiz) {
        super(raiz);
        this.q = new ModeloQuadrupedeFilhote(raiz);
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 11).addBox(-4.5F, -3.5F, -5.5F, 9.0F, 7.0F, 11.0F), PartPose.offset(0.0F, 18.5F, 2.5F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.0F, -5.0F, 7.0F, 6.0F, 5.0F).texOffs(24, 6).addBox(-2.0F, 1.0F, -6.0F, 4.0F, 2.0F, 1.0F).texOffs(24, 0).addBox(-4.5F, -4.0F, -3.5F, 3.0F, 3.0F, 1.0F).texOffs(33, 0).addBox(1.5F, -4.0F, -3.5F, 3.0F, 3.0F, 1.0F), PartPose.offset(0.0F, 19.0F, -3.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(-3.0F, 22.0F, 6.5F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(3.0F, 22.0F, 6.5F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 29).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(-3.0F, 22.0F, -1.5F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(12, 29).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(3.0F, 22.0F, -1.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void prepareMobModel(Panda entidade, float limbSwing, float limbSwingAmount, float partialTick) {
        this.sentar = entidade.getSitAmount(partialTick);
        this.deitarCostas = entidade.getLieOnBackAmount(partialTick);
    }

    @Override
    public void setupAnim(Panda entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        q.animar(limbSwing, limbSwingAmount, netHeadYaw, headPitch);
        if (entidade.getUnhappyCounter() > 0) {
            q.cabeca.yRot = 0.35F * Mth.sin(0.6F * ageInTicks);
            q.cabeca.zRot = 0.35F * Mth.sin(0.6F * ageInTicks);
            q.dianteiraDireita.xRot = -0.75F * Mth.sin(0.3F * ageInTicks);
            q.dianteiraEsquerda.xRot = 0.75F * Mth.sin(0.3F * ageInTicks);
        } else {
            q.cabeca.zRot = 0.0F;
        }
        if (entidade.isSneezing()) {
            int espirro = entidade.getSneezeCounter();
            if (espirro < 15) {
                q.cabeca.xRot = -Mth.PI / 4.0F * (float) espirro / 14.0F;
            } else if (espirro < 20) {
                float parte = (float) ((espirro - 15) / 5);
                q.cabeca.xRot = -Mth.PI / 4.0F + Mth.PI / 4.0F * parte;
            }
        }
        if (sentar > 0.0F) {
            q.corpo.xRot = ModelUtils.rotlerpRad(q.corpo.xRot, 0.17453292F, sentar);
            q.corpo.z = Mth.lerp(sentar, q.corpo.z, -1.5F);
            q.cabeca.z = Mth.lerp(sentar, q.cabeca.z, -11.5F);
            q.cabeca.y = Mth.lerp(sentar, q.cabeca.y, 17.5F);
            q.dianteiraDireita.z = Mth.lerp(sentar, q.dianteiraDireita.z, -5.0F);
            q.dianteiraEsquerda.z = Mth.lerp(sentar, q.dianteiraEsquerda.z, -5.0F);
            q.traseiraDireita.z = Mth.lerp(sentar, q.traseiraDireita.z, 3.0F);
            q.traseiraEsquerda.z = Mth.lerp(sentar, q.traseiraEsquerda.z, 3.0F);
            q.dianteiraDireita.zRot = -0.27079642F;
            q.dianteiraEsquerda.zRot = 0.27079642F;
            q.traseiraDireita.zRot = 0.5707964F;
            q.traseiraEsquerda.zRot = -0.5707964F;
            if (entidade.isEating()) {
                q.cabeca.xRot = Mth.HALF_PI + 0.2F * Mth.sin(ageInTicks * 0.6F);
                q.dianteiraDireita.xRot = -0.4F - 0.2F * Mth.sin(ageInTicks * 0.6F);
                q.dianteiraEsquerda.xRot = -0.4F - 0.2F * Mth.sin(ageInTicks * 0.6F);
            }
            if (entidade.isScared()) {
                q.cabeca.xRot = 2.1707964F;
                q.dianteiraDireita.xRot = -0.9F;
                q.dianteiraEsquerda.xRot = -0.9F;
            }
        } else {
            q.traseiraDireita.zRot = 0.0F;
            q.traseiraEsquerda.zRot = 0.0F;
            q.dianteiraDireita.zRot = 0.0F;
            q.dianteiraEsquerda.zRot = 0.0F;
        }
        if (deitarCostas > 0.0F) {
            q.traseiraDireita.xRot = -0.6F * Mth.sin(ageInTicks * 0.15F);
            q.traseiraEsquerda.xRot = 0.6F * Mth.sin(ageInTicks * 0.15F);
            q.dianteiraDireita.xRot = 0.3F * Mth.sin(ageInTicks * 0.25F);
            q.dianteiraEsquerda.xRot = -0.3F * Mth.sin(ageInTicks * 0.25F);
            q.cabeca.xRot = ModelUtils.rotlerpRad(q.cabeca.xRot, Mth.HALF_PI, deitarCostas);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int luz, int sobreposicao, int cor) {
        q.renderizar(poseStack, buffer, luz, sobreposicao, cor);
    }
}
