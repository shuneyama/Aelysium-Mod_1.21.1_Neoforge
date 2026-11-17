package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.minecraft.client.model.ModelUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Bee;

public class ModeloAbelhaFilhote extends ModeloFilhoteHierarquico<Bee> {

    private final ModelPart osso;
    private final ModelPart asaDireita;
    private final ModelPart asaEsquerda;
    private final ModelPart pernasFrente;
    private final ModelPart pernasMeio;
    private final ModelPart pernasTras;
    private final ModelPart ferrao;
    private float rolar;

    public ModeloAbelhaFilhote(ModelPart raiz) {
        super(raiz);
        this.osso = raiz.getChild("bone");
        this.ferrao = osso.getChild("body").getChild("stinger");
        this.asaDireita = osso.getChild("right_wing");
        this.asaEsquerda = osso.getChild("left_wing");
        this.pernasFrente = osso.getChild("front_legs");
        this.pernasMeio = osso.getChild("middle_legs");
        this.pernasTras = osso.getChild("back_legs");
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bone = root.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(6, 12).addBox(1.0F, -1.6667F, -2.1633F, 1.0F, 2.0F, 2.0F).texOffs(0, 12).addBox(-2.0F, -1.6667F, -2.1933F, 1.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 19.6667F, -1.8567F));
        PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.5F, 4.0F, 4.0F, 5.0F), PartPose.offset(0.0F, 1.3333F, 2.3567F));
        body.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(13, 2).addBox(0.0F, -0.5F, 0.0F, 0.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 0.5F, 2.5F));
        bone.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(3, 9).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 0.0F, 3.0F), PartPose.offsetAndRotation(-1.0F, -0.6667F, 0.8567F, 0.2182F, 0.3491F, 0.0F));
        bone.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(-3, 9).mirror().addBox(0.0F, 0.0F, 0.0F, 3.0F, 0.0F, 3.0F).mirror(false), PartPose.offsetAndRotation(1.0F, -0.6667F, 0.8567F, 0.2182F, -0.3491F, 0.0F));
        bone.addOrReplaceChild("front_legs", CubeListBuilder.create().texOffs(13, 0).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 0.0F), PartPose.offset(0.0F, 3.3333F, 1.8567F));
        bone.addOrReplaceChild("middle_legs", CubeListBuilder.create().texOffs(13, 1).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 0.0F), PartPose.offset(0.0F, 3.3333F, 2.8567F));
        bone.addOrReplaceChild("back_legs", CubeListBuilder.create().texOffs(13, 2).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 0.0F), PartPose.offset(0.0F, 3.3333F, 3.8567F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void prepareMobModel(Bee entidade, float limbSwing, float limbSwingAmount, float partialTick) {
        this.rolar = entidade.getRollAmount(partialTick);
    }

    @Override
    public void setupAnim(Bee entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        UtilFilhotes.resetar(raiz);
        ferrao.visible = !entidade.hasStung();
        boolean noChao = entidade.onGround() && entidade.getDeltaMovement().lengthSqr() < 1.0E-7;
        if (!noChao) {
            float velocidade = ageInTicks * 120.32113F * Mth.DEG_TO_RAD;
            asaDireita.yRot = 0.0F;
            asaDireita.zRot = Mth.cos(velocidade) * Mth.PI * 0.15F;
            asaEsquerda.xRot = asaDireita.xRot;
            asaEsquerda.yRot = asaDireita.yRot;
            asaEsquerda.zRot = -asaDireita.zRot;
            pernasFrente.xRot = Mth.PI / 4.0F;
            pernasMeio.xRot = Mth.PI / 4.0F;
            pernasTras.xRot = Mth.PI / 4.0F;
        }
        if (!entidade.isAngry() && !noChao) {
            float balanco = Mth.cos(ageInTicks * 0.18F);
            osso.xRot = 0.1F + balanco * Mth.PI * 0.025F;
            osso.y -= Mth.cos(ageInTicks * 0.18F) * 0.9F;
            pernasFrente.xRot = -balanco * Mth.PI * 0.1F + Mth.PI / 8.0F;
            pernasTras.xRot = -balanco * Mth.PI * 0.05F + Mth.PI / 4.0F;
        }
        if (rolar > 0.0F) {
            osso.xRot = ModelUtils.rotlerpRad(osso.xRot, 3.0915928F, rolar);
        }
    }
}
