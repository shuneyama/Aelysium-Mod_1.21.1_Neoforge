package net.aelysium.aelysiummod.sistemas.mundo.jardimpalido.cliente;

import net.aelysium.aelysiummod.sistemas.mundo.jardimpalido.Creaking;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModeloCreaking extends HierarchicalModel<Creaking> {

    private final ModelPart raiz;
    private final ModelPart tronco;
    private final ModelPart cabeca;
    private final ModelPart corpo;
    private final ModelPart bracoDireito;
    private final ModelPart bracoEsquerdo;
    private final ModelPart pernaDireita;
    private final ModelPart pernaEsquerda;

    public ModeloCreaking(ModelPart raiz) {
        this.raiz = raiz;
        ModelPart base = raiz.getChild("root");
        this.tronco = base.getChild("upper_body");
        this.cabeca = tronco.getChild("head");
        this.corpo = tronco.getChild("body");
        this.bracoDireito = tronco.getChild("right_arm");
        this.bracoEsquerdo = tronco.getChild("left_arm");
        this.pernaEsquerda = base.getChild("left_leg");
        this.pernaDireita = base.getChild("right_leg");
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition base = malha.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition tronco = base.addOrReplaceChild("upper_body", CubeListBuilder.create(), PartPose.offset(-1.0F, -19.0F, 0.0F));
        tronco.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -10.0F, -3.0F, 6.0F, 10.0F, 6.0F)
                .texOffs(28, 31).addBox(-3.0F, -13.0F, -3.0F, 6.0F, 3.0F, 6.0F)
                .texOffs(12, 40).addBox(3.0F, -13.0F, 0.0F, 9.0F, 14.0F, 0.0F)
                .texOffs(34, 12).addBox(-12.0F, -14.0F, 0.0F, 9.0F, 14.0F, 0.0F), PartPose.offset(-3.0F, -11.0F, 0.0F));
        tronco.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(0.0F, -3.0F, -3.0F, 6.0F, 13.0F, 5.0F)
                .texOffs(24, 0).addBox(-6.0F, -4.0F, -3.0F, 6.0F, 7.0F, 5.0F), PartPose.offset(0.0F, -7.0F, 1.0F));
        tronco.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(22, 13).addBox(-2.0F, -1.5F, -1.5F, 3.0F, 21.0F, 3.0F)
                .texOffs(46, 0).addBox(-2.0F, 19.5F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(-7.0F, -9.5F, 1.5F));
        tronco.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(30, 40).addBox(0.0F, -1.0F, -1.5F, 3.0F, 16.0F, 3.0F)
                .texOffs(52, 12).addBox(0.0F, -5.0F, -1.5F, 3.0F, 4.0F, 3.0F)
                .texOffs(52, 19).addBox(0.0F, 15.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(6.0F, -9.0F, 0.5F));
        base.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(42, 40).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 16.0F, 3.0F)
                .texOffs(45, 55).addBox(-1.5F, 15.7F, -4.5F, 5.0F, 0.0F, 9.0F), PartPose.offset(1.5F, -16.0F, 0.5F));
        base.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 34).addBox(-3.0F, -1.5F, -1.5F, 3.0F, 19.0F, 3.0F)
                .texOffs(45, 46).addBox(-5.0F, 17.2F, -4.5F, 5.0F, 0.0F, 9.0F)
                .texOffs(12, 34).addBox(-3.0F, -4.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(-1.0F, -17.5F, 0.5F));
        return LayerDefinition.create(malha, 64, 64);
    }

    public void somenteCabeca(boolean somente) {
        corpo.skipDraw = somente;
        bracoDireito.skipDraw = somente;
        bracoEsquerdo.skipDraw = somente;
        pernaDireita.skipDraw = somente;
        pernaEsquerda.skipDraw = somente;
    }

    @Override
    public ModelPart root() {
        return raiz;
    }

    @Override
    public void setupAnim(Creaking entidade, float balanco, float velocidade, float idade, float guinada, float arfagem) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        cabeca.yRot = guinada * ((float) Math.PI / 180.0F);
        cabeca.xRot = arfagem * ((float) Math.PI / 180.0F);
        if (entidade.podeMover()) {
            float balancar = Mth.cos(balanco * 0.6662F) * 1.2F * velocidade;
            pernaEsquerda.xRot = balancar;
            pernaDireita.xRot = -balancar;
            bracoEsquerdo.xRot = -balancar * 0.6F;
            bracoDireito.xRot = balancar * 0.6F;
            tronco.zRot = Mth.cos(balanco * 0.3331F) * 0.08F * velocidade;
            if (entidade.tempoAtaque > 0) {
                float levantar = Mth.sin((1.0F - entidade.tempoAtaque / 15.0F) * (float) Math.PI);
                bracoDireito.xRot = -1.8F * levantar;
                bracoEsquerdo.xRot = -1.8F * levantar;
            }
        }
        if (entidade.tempoTorcer > 0) {
            tronco.zRot += Mth.sin(idade * 2.5F) * 0.12F;
            cabeca.zRot += Mth.sin(idade * 3.0F) * 0.1F;
        }
    }
}
