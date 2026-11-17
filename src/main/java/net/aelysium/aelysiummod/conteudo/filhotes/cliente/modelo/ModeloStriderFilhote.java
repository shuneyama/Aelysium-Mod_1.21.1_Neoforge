package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Strider;

public class ModeloStriderFilhote extends ModeloFilhoteHierarquico<Strider> {

    private final ModelPart pernaDireita;
    private final ModelPart pernaEsquerda;
    private final ModelPart corpo;
    private final ModelPart cerdaFrente;
    private final ModelPart cerdaMeio;
    private final ModelPart cerdaTras;

    public ModeloStriderFilhote(ModelPart raiz) {
        super(raiz);
        this.pernaDireita = raiz.getChild("right_leg");
        this.pernaEsquerda = raiz.getChild("left_leg");
        this.corpo = raiz.getChild("body");
        this.cerdaFrente = corpo.getChild("bristle2");
        this.cerdaMeio = corpo.getChild("bristle1");
        this.cerdaTras = corpo.getChild("bristle0");
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.75F, -4.0F, 7.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.75F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 20.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(8, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 20.0F, 0.0F));
        body.addOrReplaceChild("bristle0", CubeListBuilder.create().texOffs(0, 21).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.25F, 2.0F));
        body.addOrReplaceChild("bristle1", CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.25F, 0.0F));
        body.addOrReplaceChild("bristle2", CubeListBuilder.create().texOffs(0, 15).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.25F, -2.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(Strider entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        UtilFilhotes.resetar(raiz);
        float posicao = UtilFilhotes.posicaoAndar(limbSwing);
        float velocidade = Math.min(limbSwingAmount, 0.25F);
        if (!entidade.isVehicle()) {
            corpo.xRot = headPitch * Mth.DEG_TO_RAD;
            corpo.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        } else {
            corpo.xRot = 0.0F;
            corpo.yRot = 0.0F;
        }
        corpo.zRot = 0.1F * Mth.sin(posicao * 1.5F) * 4.0F * velocidade;
        pernaEsquerda.xRot = Mth.sin(posicao * 1.5F * 0.5F) * 2.0F * velocidade;
        pernaDireita.xRot = Mth.sin(posicao * 1.5F * 0.5F + Mth.PI) * 2.0F * velocidade;
        pernaEsquerda.zRot = 0.17453292F * Mth.cos(posicao * 1.5F * 0.5F) * velocidade;
        pernaDireita.zRot = 0.17453292F * Mth.cos(posicao * 1.5F * 0.5F + Mth.PI) * velocidade;
        corpo.y = 17.25F;
        corpo.y -= 1.0F * Mth.cos(posicao * 1.5F) * 2.0F * velocidade;
        pernaEsquerda.y = 20.0F + 2.0F * Mth.sin(posicao * 1.5F * 0.5F + Mth.PI) * 2.0F * velocidade;
        pernaDireita.y = 20.0F + 2.0F * Mth.sin(posicao * 1.5F * 0.5F) * 2.0F * velocidade;
        float fluxo = Mth.cos(posicao * 1.5F + Mth.PI) * velocidade;
        cerdaFrente.xRot += fluxo * 0.6F;
        cerdaMeio.xRot += fluxo * 1.2F;
        cerdaTras.xRot += fluxo * 1.3F;
        cerdaFrente.xRot += 0.1F * Mth.sin(ageInTicks * 0.4F);
        cerdaMeio.xRot += 0.1F * Mth.sin(ageInTicks * 0.2F);
        cerdaTras.xRot += 0.05F * Mth.sin(ageInTicks * -0.4F);
    }
}
