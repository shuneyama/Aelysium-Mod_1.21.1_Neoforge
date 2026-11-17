package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Wolf;

public class ModeloLoboFilhote extends WolfModel<Wolf> {

    private final ModeloQuadrupedeFilhote q;
    private final ModelPart cauda;
    private float parcial;
    private int corModelo = -1;

    public ModeloLoboFilhote(ModelPart raiz) {
        super(raiz);
        this.q = new ModeloQuadrupedeFilhote(raiz);
        this.cauda = raiz.getChild("tail");
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 12).addBox(-2.99F, -3.25F, -3.0F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.025F)).texOffs(17, 12).addBox(-1.5F, -0.24F, -5.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 18.25F, -4.0F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(0, 5).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(-2.0F, -4.25F, -0.5F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(20, 5).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(2.0F, -4.25F, -0.5F));
        UtilFilhotes.vazias(head, "real_head");
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 4.0F, 8.0F), PartPose.offset(0.0F, 19.0F, 0.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(-1.5F, 21.0F, 3.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(8, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(1.5F, 21.0F, 3.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(-1.5F, 21.0F, -3.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(20, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(1.5F, 21.0F, -3.0F));
        PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 19.0F, 3.0F, -0.5236F, 0.0F, 0.0F));
        tail.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(22, 16).addBox(-1.0F, -5.7F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.6F, 0.2F, -3.1F, 0.0F, 0.0F));
        UtilFilhotes.vazias(tail, "real_tail");
        UtilFilhotes.vazias(root, "upper_body");
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void prepareMobModel(Wolf entidade, float limbSwing, float limbSwingAmount, float partialTick) {
        this.parcial = partialTick;
    }

    @Override
    public void setupAnim(Wolf entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        UtilFilhotes.resetar(q.raiz);
        float posicao = UtilFilhotes.posicaoAndar(limbSwing);
        cauda.yRot = entidade.isAngry() ? 0.0F : Mth.cos(posicao * 0.6662F) * 1.4F * limbSwingAmount;
        if (entidade.isInSittingPose()) {
            sentar(entidade.getAgeScale());
        } else {
            UtilFilhotes.pernas(q.traseiraDireita, q.traseiraEsquerda, q.dianteiraDireita, q.dianteiraEsquerda, posicao, limbSwingAmount);
        }
        q.corpo.zRot = entidade.getBodyRollAngle(parcial, -0.16F);
        q.cabeca.zRot = entidade.getHeadRollAngle(parcial) + entidade.getBodyRollAngle(parcial, 0.0F);
        cauda.zRot = entidade.getBodyRollAngle(parcial, -0.2F);
        UtilFilhotes.cabeca(q.cabeca, headPitch, netHeadYaw);
        cauda.xRot = ageInTicks;
    }

    private void sentar(float escala) {
        q.corpo.y += 4.0F * escala;
        q.corpo.z -= 2.0F * escala;
        q.corpo.xRot = Mth.PI / 4.0F - 1.0F;
        cauda.y += 9.0F * escala;
        cauda.z -= 2.0F * escala;
        q.traseiraDireita.y += 6.7F * escala;
        q.traseiraDireita.z -= 5.0F * escala;
        q.traseiraDireita.xRot = Mth.PI * 1.5F;
        q.traseiraEsquerda.y += 6.7F * escala;
        q.traseiraEsquerda.z -= 5.0F * escala;
        q.traseiraEsquerda.xRot = Mth.PI * 1.5F;
        q.dianteiraDireita.xRot = 5.811947F;
        q.dianteiraDireita.x += 0.01F * escala;
        q.dianteiraDireita.y += 1.0F * escala;
        q.dianteiraEsquerda.xRot = 5.811947F;
        q.dianteiraEsquerda.x -= 0.01F * escala;
        q.dianteiraEsquerda.y += 1.0F * escala;
    }

    @Override
    public void setColor(int cor) {
        super.setColor(cor);
        this.corModelo = cor;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int luz, int sobreposicao, int cor) {
        q.renderizar(poseStack, buffer, luz, sobreposicao, FastColor.ARGB32.multiply(cor, corModelo));
    }
}
