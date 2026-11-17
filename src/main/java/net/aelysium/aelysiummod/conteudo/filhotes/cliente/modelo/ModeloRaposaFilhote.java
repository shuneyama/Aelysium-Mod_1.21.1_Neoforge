package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.animacao.FoxBabyAnimation;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Fox;

public class ModeloRaposaFilhote extends FoxModel<Fox> {

    private final ModeloQuadrupedeFilhote q;
    private final ModelPart cauda;
    private final AnimacaoFilhote andar;
    private float parcial;
    private float movimentoPernas;

    public ModeloRaposaFilhote(ModelPart raiz) {
        super(raiz);
        this.q = new ModeloQuadrupedeFilhote(raiz);
        this.cauda = q.corpo.getChild("tail");
        this.andar = AnimacaoFilhote.preparar(raiz, FoxBabyAnimation.FOX_BABY_WALK);
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.125F, -5.125F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(18, 20).addBox(-1.0F, 0.875F, -7.125F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(22, 8).addBox(-3.0F, -4.125F, -4.125F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(22, 11).addBox(1.0F, -4.125F, -4.125F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 18.125F, 0.125F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(22, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 22.0F, 4.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 22.0F, 4.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(22, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 22.0F, 0.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 22.0F, 0.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, 2.0F));
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 20).addBox(-1.5F, -1.48F, -1.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, 3.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void prepareMobModel(Fox entidade, float limbSwing, float limbSwingAmount, float partialTick) {
        this.parcial = partialTick;
    }

    @Override
    public void setupAnim(Fox entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        UtilFilhotes.resetar(q.raiz);
        float escala = entidade.getAgeScale();
        boolean dormindo = entidade.isSleeping();
        boolean caido = entidade.isFaceplanted();
        boolean agachado = entidade.isCrouching();
        q.cabeca.zRot = entidade.getHeadRollAngle(parcial);
        q.traseiraDireita.visible = true;
        q.traseiraEsquerda.visible = true;
        q.dianteiraDireita.visible = true;
        q.dianteiraEsquerda.visible = true;
        andar.andar(UtilFilhotes.posicaoAndar(limbSwing), limbSwingAmount, 1.0F, 2.5F);
        if (agachado) {
            float agachar = entidade.getCrouchAmount(parcial);
            q.corpo.xRot += 0.10471976F;
            q.cabeca.y += agachar * escala;
            float balanco = Mth.cos(ageInTicks) * 0.05F;
            q.corpo.yRot = balanco;
            q.traseiraDireita.zRot = balanco;
            q.traseiraEsquerda.zRot = balanco;
            q.dianteiraDireita.zRot = balanco / 2.0F;
            q.dianteiraEsquerda.zRot = balanco / 2.0F;
            q.corpo.y += agachar / 6.0F;
        } else if (dormindo) {
            q.traseiraDireita.visible = false;
            q.traseiraEsquerda.visible = false;
            q.dianteiraDireita.visible = false;
            q.dianteiraEsquerda.visible = false;
            q.corpo.zRot = -Mth.HALF_PI;
            q.corpo.xRot = -0.17453292F;
            ++q.corpo.y;
            --q.corpo.z;
            --q.corpo.x;
            cauda.xRot = -2.1816616F;
            cauda.x -= 0.7F;
            cauda.z += 0.6F;
            cauda.y += 0.9F;
            q.cabeca.x -= 2.0F;
            q.cabeca.y += 2.8F;
            q.cabeca.z -= 4.0F;
            q.cabeca.yRot = -2.0943952F;
            q.cabeca.zRot = 0.0F;
        } else if (entidade.isSitting()) {
            q.cabeca.xRot = 0.0F;
            q.cabeca.yRot = 0.0F;
            q.corpo.xRot = -0.959931F;
            q.corpo.z -= 4.5F * escala;
            q.corpo.y += 3.0F * escala;
            cauda.y -= 0.6F;
            cauda.z -= 2.0F * escala;
            cauda.xRot = 0.95993114F;
            q.cabeca.y -= 0.75F;
            q.dianteiraDireita.xRot = -0.2617994F;
            q.dianteiraEsquerda.xRot = -0.2617994F;
            --q.dianteiraDireita.z;
            --q.dianteiraEsquerda.z;
            q.dianteiraDireita.x += 0.01F;
            q.dianteiraEsquerda.x -= 0.01F;
            q.traseiraDireita.z -= 3.75F;
            q.traseiraEsquerda.z -= 3.75F;
            q.traseiraDireita.x += 0.01F;
            q.traseiraEsquerda.x -= 0.01F;
        }
        if (!dormindo && !caido && !agachado) {
            UtilFilhotes.cabeca(q.cabeca, headPitch, netHeadYaw);
        }
        if (dormindo) {
            q.cabeca.xRot = 0.0F;
            q.cabeca.yRot = -2.0943952F;
            q.cabeca.zRot = Mth.cos(ageInTicks * 0.027F) / 22.0F;
        }
        if (caido) {
            movimentoPernas += 0.67F;
            q.traseiraDireita.xRot = Mth.cos(movimentoPernas * 0.4662F) * 0.1F;
            q.traseiraEsquerda.xRot = Mth.cos(movimentoPernas * 0.4662F + Mth.PI) * 0.1F;
            q.dianteiraDireita.xRot = Mth.cos(movimentoPernas * 0.4662F + Mth.PI) * 0.1F;
            q.dianteiraEsquerda.xRot = Mth.cos(movimentoPernas * 0.4662F) * 0.1F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int luz, int sobreposicao, int cor) {
        q.renderizar(poseStack, buffer, luz, sobreposicao, cor);
    }
}
