package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class ModeloBurroFilhote<T extends AbstractHorse> extends ModeloEquinoFilhote<T> {

    public ModeloBurroFilhote(ModelPart raiz) {
        super(raiz, raiz.getChild("body").getChild("head_parts"), raiz.getChild("body").getChild("right_hind_leg"), raiz.getChild("body").getChild("right_front_leg"), raiz.getChild("body").getChild("left_hind_leg"), raiz.getChild("body").getChild("left_front_leg"), raiz.getChild("body").getChild("tail"));
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 13).addBox(-5.0F, -3.0F, -7.0F, 8.0F, 6.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 14.0F, 0.0F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5F, 6.5F));
        tail.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(24, 33).addBox(-2.5F, -1.0F, -0.5F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7418F, 0.0F, 0.0F));
        body.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(12, 44).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.25F, 3.5F, 5.25F));
        body.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 44).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.4F, 3.5F, 5.4F));
        body.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(12, 33).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.4F, 3.5F, -5.3F));
        body.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.4F, 3.5F, -5.4F));
        PartDefinition neck = body.addOrReplaceChild("head_parts", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, -5.0F));
        neck.addOrReplaceChild("neck_r1", CubeListBuilder.create().texOffs(30, 9).addBox(-3.0F, -6.0F, -3.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, -3.0F));
        head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.6F, -8.4F, 6.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 1.0F, 0.3927F, 0.0F, 0.0F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.5F, -0.3F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -3.5F, -1.0F, 0.48F, 0.0F, 0.48F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(22, 0).mirror().addBox(-2.0F, -6.5F, -0.3F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, -3.5F, -1.0F, 0.48F, 0.0F, -0.48F));
        body.addOrReplaceChild("right_chest", CubeListBuilder.create(), PartPose.offset(-1.0F, 10.0F, 0.0F));
        body.addOrReplaceChild("left_chest", CubeListBuilder.create(), PartPose.offset(-1.0F, 10.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entidade, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float pitchRad = -30.0F * Mth.DEG_TO_RAD;
        float anguloBase = (1.0F - Math.max(empinar, comer)) * (Mth.PI / 6.0F + pitchRad + mastigar * Mth.sin(ageInTicks) * 0.05F);
        partesCabeca.xRot = empinar * (0.2617994F + pitchRad) + comer * (Mth.HALF_PI + Mth.sin(ageInTicks) * 0.05F) + anguloBase;
    }

    @Override
    protected void ajustarPernasEmpinado(float empinar) {
        traseiraEsquerda.y = Mth.lerp(empinar, traseiraEsquerda.y, -0.3F);
        traseiraDireita.y = Mth.lerp(empinar, traseiraEsquerda.y, -0.3F);
    }

    @Override
    protected float anguloPernaEmpinado() {
        return Mth.PI / 3.0F;
    }

    @Override
    protected float deslocamentoYEmpinado() {
        return 1.0F;
    }

    @Override
    protected float deslocamentoZEmpinado() {
        return 0.5F;
    }

    @Override
    protected float rotacaoXPernaEmpinada() {
        return 0.0F;
    }

    @Override
    protected float rotacaoCauda() {
        return -Mth.PI / 4.0F;
    }

    @Override
    protected void posicionarCabeca(float comer, float empinar) {
        partesCabeca.y = Mth.lerp(comer, partesCabeca.y, -1.2F);
        partesCabeca.z = Mth.lerp(empinar, partesCabeca.z, -3.6F);
    }
}
