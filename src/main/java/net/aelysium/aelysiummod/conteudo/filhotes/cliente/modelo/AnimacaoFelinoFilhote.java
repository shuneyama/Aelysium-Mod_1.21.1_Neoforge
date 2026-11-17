package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class AnimacaoFelinoFilhote {

    public final ModelPart raiz;
    private final ModelPart cabeca;
    private final ModelPart corpo;
    private final ModelPart cauda1;
    private final ModelPart cauda2;
    private final ModelPart traseiraEsquerda;
    private final ModelPart traseiraDireita;
    private final ModelPart dianteiraEsquerda;
    private final ModelPart dianteiraDireita;

    public AnimacaoFelinoFilhote(ModelPart raiz) {
        this.raiz = raiz;
        this.cabeca = raiz.getChild("head");
        this.corpo = raiz.getChild("body");
        this.cauda1 = raiz.getChild("tail1");
        this.cauda2 = raiz.getChild("tail2");
        this.traseiraEsquerda = raiz.getChild("left_hind_leg");
        this.traseiraDireita = raiz.getChild("right_hind_leg");
        this.dianteiraEsquerda = raiz.getChild("left_front_leg");
        this.dianteiraDireita = raiz.getChild("right_front_leg");
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -3.0F, -2.875F, 5.0F, 4.0F, 4.0F).texOffs(18, 0).addBox(-2.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F).texOffs(24, 0).addBox(1.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F).texOffs(18, 3).addBox(-1.5F, -1.0F, -3.875F, 3.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 20.0F, -3.125F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(18, 18).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(1.0F, 22.0F, -1.5F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(12, 18).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(-1.0F, 22.0F, -1.5F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(18, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(1.0F, 22.0F, 2.5F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -1.5F, -3.5F, 4.0F, 3.0F, 7.0F), PartPose.offset(0.0F, 20.5F, 0.5F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(12, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(-1.0F, 22.0F, 2.5F));
        root.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 18).addBox(-0.5F, -0.107F, 0.0849F, 1.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 19.107F, 3.9151F, -0.567232F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail2", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    public void animar(boolean agachado, boolean correndo, boolean sentado, float deitar, float deitarCauda, float relaxar, float escala, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch) {
        UtilFilhotes.resetar(raiz);
        if (agachado) {
            corpo.y += 1.0F * escala;
            cabeca.y += 2.0F * escala;
            cauda1.y += 1.0F * escala;
            cauda2.y += -4.0F * escala;
            cauda2.z += 2.0F * escala;
            cauda1.xRot = Mth.HALF_PI;
            cauda2.xRot = Mth.HALF_PI;
        } else if (correndo) {
            cauda2.y = cauda1.y;
            cauda2.z += 2.0F * escala;
            cauda1.xRot = Mth.HALF_PI;
            cauda2.xRot = Mth.HALF_PI;
        }
        UtilFilhotes.cabeca(cabeca, headPitch, netHeadYaw);
        float posicao = UtilFilhotes.posicaoAndar(limbSwing);
        if (!sentado) {
            if (correndo) {
                traseiraEsquerda.xRot = Mth.cos(posicao * 0.6662F) * limbSwingAmount;
                traseiraDireita.xRot = Mth.cos(posicao * 0.6662F + 0.3F) * limbSwingAmount;
                dianteiraEsquerda.xRot = Mth.cos(posicao * 0.6662F + Mth.PI + 0.3F) * limbSwingAmount;
                dianteiraDireita.xRot = Mth.cos(posicao * 0.6662F + Mth.PI) * limbSwingAmount;
                cauda2.xRot = 1.7278761F + Mth.PI / 10.0F * Mth.cos(posicao) * limbSwingAmount;
            } else {
                traseiraEsquerda.xRot = Mth.cos(posicao * 0.6662F) * limbSwingAmount;
                traseiraDireita.xRot = Mth.cos(posicao * 0.6662F + Mth.PI) * limbSwingAmount;
                dianteiraEsquerda.xRot = Mth.cos(posicao * 0.6662F + Mth.PI) * limbSwingAmount;
                dianteiraDireita.xRot = Mth.cos(posicao * 0.6662F) * limbSwingAmount;
                if (!agachado) {
                    cauda2.xRot = 1.7278761F + Mth.PI / 4.0F * Mth.cos(posicao) * limbSwingAmount;
                } else {
                    cauda2.xRot = 1.7278761F + 0.47123894F * Mth.cos(posicao) * limbSwingAmount;
                }
            }
        } else {
            corpo.xRot += -0.43633232F;
            ++corpo.y;
            cabeca.z += 0.75F;
            cauda1.xRot += 0.5454154F;
            cauda1.y += 4.0F;
            cauda1.z -= 0.9F;
            traseiraEsquerda.z -= 0.9F;
            traseiraDireita.z -= 0.9F;
        }
        if (deitar > 0.0F) {
            ++corpo.x;
            cabeca.xRot = Mth.rotLerp(deitar, cabeca.xRot, 0.17453292F);
            cabeca.zRot = Mth.rotLerp(deitar, cabeca.zRot, -1.3089969F);
            ++cabeca.x;
            cabeca.y += 0.75F;
            cabeca.z -= 0.5F;
            dianteiraDireita.xRot = -Mth.PI / 4.0F;
            dianteiraDireita.x += 3.5F;
            dianteiraDireita.y -= 0.5F;
            dianteiraEsquerda.xRot = -Mth.HALF_PI;
            ++dianteiraEsquerda.x;
            --dianteiraEsquerda.y;
            dianteiraEsquerda.z -= 2.0F;
            traseiraDireita.xRot = 0.6981317F;
            traseiraDireita.yRot = 0.34906584F;
            traseiraDireita.zRot = -0.34906584F;
            traseiraDireita.x += 2.5F;
            traseiraDireita.y -= 0.25F;
            traseiraDireita.z += 0.5F;
            ++traseiraEsquerda.x;
            --traseiraEsquerda.z;
            cauda1.xRot += Mth.rotLerp(deitarCauda, cauda1.xRot, -Mth.PI / 6.0F);
            cauda1.yRot += Mth.rotLerp(deitarCauda, cauda1.yRot, 0.0F);
            cauda1.zRot += Mth.rotLerp(deitarCauda, cauda1.zRot, -0.17453292F);
            ++cauda1.x;
            cauda1.y += 0.5F;
            cauda1.z -= 0.25F;
        }
        if (relaxar > 0.0F) {
            cabeca.xRot = Mth.rotLerp(relaxar, cabeca.xRot, -0.58177644F);
        }
    }
}
