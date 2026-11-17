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
import net.minecraft.world.entity.animal.horse.Llama;

public class ModeloLhamaFilhote extends ModeloFilhoteHierarquico<Llama> {

    private final ModelPart cabeca;
    private final ModelPart traseiraDireita;
    private final ModelPart traseiraEsquerda;
    private final ModelPart dianteiraDireita;
    private final ModelPart dianteiraEsquerda;
    private final ModelPart bauDireito;
    private final ModelPart bauEsquerdo;

    public ModeloLhamaFilhote(ModelPart raiz) {
        super(raiz);
        this.cabeca = raiz.getChild("head");
        this.bauDireito = raiz.getChild("right_chest");
        this.bauEsquerdo = raiz.getChild("left_chest");
        this.traseiraDireita = raiz.getChild("right_hind_leg");
        this.traseiraEsquerda = raiz.getChild("left_hind_leg");
        this.dianteiraDireita = raiz.getChild("right_front_leg");
        this.dianteiraEsquerda = raiz.getChild("left_front_leg");
    }

    public static LayerDefinition criarCamada() {
        return criarCamada(CubeDeformation.NONE);
    }

    public static LayerDefinition criarCamadaDecoracao() {
        return criarCamada(new CubeDeformation(0.2F));
    }

    private static LayerDefinition criarCamada(CubeDeformation g) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -9.0F, -4.0F, 6.0F, 11.0F, 4.0F, g).texOffs(0, 15).addBox(-1.5F, -7.0F, -7.0F, 3.0F, 3.0F, 3.0F, g).texOffs(20, 4).addBox(0.5F, -11.0F, -3.0F, 2.0F, 2.0F, 2.0F, g).texOffs(20, 0).addBox(-2.5F, -11.0F, -3.0F, 2.0F, 2.0F, 2.0F, g), PartPose.offset(0.0F, 12.0F, -4.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 45).addBox(-1.4F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(-2.5F, 16.5F, 4.5F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(12, 45).addBox(-1.6F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(2.5F, 16.5F, 4.5F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-1.4F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(-2.5F, 16.5F, -3.5F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.6F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(2.5F, 16.5F, -3.5F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 15).addBox(-4.0F, -3.0F, -8.5F, 8.0F, 6.0F, 13.0F, g), PartPose.offset(0.0F, 14.0F, 2.5F));
        root.addOrReplaceChild("right_chest", CubeListBuilder.create().texOffs(45, 28).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, g), PartPose.offsetAndRotation(-8.5F, 4.0F, 3.0F, 0.0F, Mth.HALF_PI, 0.0F));
        root.addOrReplaceChild("left_chest", CubeListBuilder.create().texOffs(45, 41).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, g), PartPose.offsetAndRotation(5.5F, 4.0F, 3.0F, 0.0F, Mth.HALF_PI, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(Llama entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        UtilFilhotes.resetar(raiz);
        UtilFilhotes.cabeca(cabeca, headPitch, netHeadYaw);
        UtilFilhotes.pernas(traseiraDireita, traseiraEsquerda, dianteiraDireita, dianteiraEsquerda, UtilFilhotes.posicaoAndar(limbSwing), limbSwingAmount);
        bauDireito.visible = false;
        bauEsquerdo.visible = false;
    }
}
