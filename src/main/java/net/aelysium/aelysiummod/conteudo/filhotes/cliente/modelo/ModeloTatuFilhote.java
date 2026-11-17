package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.animacao.BabyArmadilloAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.armadillo.Armadillo;

public class ModeloTatuFilhote extends ModeloFilhoteHierarquico<Armadillo> {

    private final ModelPart corpo;
    private final ModelPart traseiraDireita;
    private final ModelPart traseiraEsquerda;
    private final ModelPart cabeca;
    private final ModelPart cauda;
    private final ModelPart cubo;
    private final AnimacaoFilhote andar;
    private final AnimacaoFilhote desenrolar;
    private final AnimacaoFilhote enrolar;
    private final AnimacaoFilhote espiar;

    public ModeloTatuFilhote(ModelPart raiz) {
        super(raiz);
        this.corpo = raiz.getChild("body");
        this.traseiraDireita = raiz.getChild("right_hind_leg");
        this.traseiraEsquerda = raiz.getChild("left_hind_leg");
        this.cabeca = corpo.getChild("head");
        this.cauda = corpo.getChild("tail");
        this.cubo = raiz.getChild("cube");
        this.andar = AnimacaoFilhote.preparar(raiz, BabyArmadilloAnimation.ARMADILLO_BABY_WALK);
        this.desenrolar = AnimacaoFilhote.preparar(raiz, BabyArmadilloAnimation.ARMADILLO_BABY_ROLL_OUT);
        this.enrolar = AnimacaoFilhote.preparar(raiz, BabyArmadilloAnimation.ARMADILLO_BABY_ROLL_UP);
        this.espiar = AnimacaoFilhote.preparar(raiz, BabyArmadilloAnimation.ARMADILLO_BABY_PEEK);
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.0F, -3.5F, 5.0F, 4.0F, 7.0F, new CubeDeformation(0.3F)).texOffs(0, 11).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F), PartPose.offset(0.0F, 20.0F, 0.5F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 3.4F));
        tail.addOrReplaceChild("right_ear_cube", CubeListBuilder.create().texOffs(22, 11).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 1.5F, 1.0F, -1.0472F, 0.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -3.2F));
        PartDefinition headGroup = head.addOrReplaceChild("head_cube", CubeListBuilder.create().texOffs(20, 17).addBox(-1.0F, -2.0F, -4.0F, 2.0F, 2.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7417649F, 0.0F, 0.0F));
        headGroup.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(28, 8).mirror().addBox(-1.8F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F).mirror(false), PartPose.offsetAndRotation(-1.0F, -2.0F, -0.3F, -0.4363F, -0.1134F, 0.0524F));
        headGroup.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(28, 8).addBox(-0.2F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F), PartPose.offsetAndRotation(1.0F, -2.0F, -0.3F, -0.4363F, 0.1134F, -0.0524F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(20, 27).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F).mirror(false), PartPose.offset(-1.5F, 22.0F, 2.5F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(20, 27).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(1.5F, 22.0F, 2.5F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(20, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(1.5F, 22.0F, -1.5F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(24, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F).mirror(false), PartPose.offset(-1.5F, 22.0F, -1.5F));
        root.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 25).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, 20.7F, 0.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(Armadillo entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        UtilFilhotes.resetar(raiz);
        boolean escondido = entidade.shouldHideInShell();
        if (escondido) {
            corpo.skipDraw = true;
            traseiraEsquerda.visible = false;
            traseiraDireita.visible = false;
            cauda.visible = false;
            cubo.visible = true;
        } else {
            corpo.skipDraw = false;
            traseiraEsquerda.visible = true;
            traseiraDireita.visible = true;
            cauda.visible = true;
            cubo.visible = false;
            cabeca.xRot = Mth.clamp(headPitch, -22.5F, 25.0F) * Mth.DEG_TO_RAD;
            cabeca.yRot = Mth.clamp(netHeadYaw, -32.5F, 32.5F) * Mth.DEG_TO_RAD;
            andar.andar(UtilFilhotes.posicaoAndar(limbSwing), limbSwingAmount, 16.5F, 2.5F);
        }
        desenrolar.estado(entidade.rollOutAnimationState, ageInTicks);
        enrolar.estado(entidade.rollUpAnimationState, ageInTicks);
        espiar.estado(entidade.peekAnimationState, ageInTicks);
    }
}
