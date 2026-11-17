package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.animacao.CamelBabyAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.camel.Camel;

public class ModeloCameloFilhote extends ModeloFilhoteHierarquico<Camel> {

    private final ModelPart cabeca;
    private final AnimacaoFilhote andar;
    private final AnimacaoFilhote sentar;
    private final AnimacaoFilhote poseSentado;
    private final AnimacaoFilhote levantar;
    private final AnimacaoFilhote ocioso;
    private final AnimacaoFilhote disparar;

    public ModeloCameloFilhote(ModelPart raiz) {
        super(raiz);
        this.cabeca = raiz.getChild("body").getChild("head");
        this.andar = AnimacaoFilhote.preparar(raiz, CamelBabyAnimation.CAMEL_BABY_WALK);
        this.sentar = AnimacaoFilhote.preparar(raiz, CamelBabyAnimation.CAMEL_BABY_SIT);
        this.poseSentado = AnimacaoFilhote.preparar(raiz, CamelBabyAnimation.CAMEL_BABY_SIT_POSE);
        this.levantar = AnimacaoFilhote.preparar(raiz, CamelBabyAnimation.CAMEL_BABY_STANDUP);
        this.ocioso = AnimacaoFilhote.preparar(raiz, CamelBabyAnimation.CAMEL_BABY_IDLE);
        this.disparar = AnimacaoFilhote.preparar(raiz, CamelBabyAnimation.CAMEL_BABY_DASH);
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 14).addBox(-4.5F, -4.0F, -8.0F, 9.0F, 8.0F, 16.0F), PartPose.offset(0.0F, 7.0F, 0.0F));
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(50, 38).addBox(-1.5F, -0.5F, 0.0F, 3.0F, 9.0F, 0.0F), PartPose.offset(0.0F, -1.5F, 8.05F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(20, 0).addBox(-2.5F, -3.0F, -7.5F, 5.0F, 5.0F, 7.0F).texOffs(0, 0).addBox(-2.5F, -12.0F, -7.5F, 5.0F, 9.0F, 5.0F).texOffs(0, 14).addBox(-2.5F, -12.0F, -10.5F, 5.0F, 4.0F, 3.0F), PartPose.offset(0.0F, 1.0F, -7.5F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(37, 0).addBox(-3.0F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(-2.5F, -11.0F, -4.0F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(47, 0).addBox(0.0F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(2.5F, -11.0F, -4.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(36, 14).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(-3.0F, 11.5F, -5.5F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(48, 14).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(3.0F, 11.5F, -5.5F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(12, 38).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(3.0F, 11.5F, 5.5F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(-3.0F, 11.5F, 5.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(Camel entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        UtilFilhotes.resetar(raiz);
        float yaw = Mth.clamp(netHeadYaw, -30.0F, 30.0F);
        float pitch = Mth.clamp(headPitch, -25.0F, 45.0F);
        if (entidade.getJumpCooldown() > 0) {
            float parcial = ageInTicks - (float) entidade.tickCount;
            float extra = 45.0F * ((float) entidade.getJumpCooldown() - parcial) / 55.0F;
            pitch = Mth.clamp(pitch + extra, -25.0F, 70.0F);
        }
        cabeca.yRot = yaw * Mth.DEG_TO_RAD;
        cabeca.xRot = pitch * Mth.DEG_TO_RAD;
        andar.andar(UtilFilhotes.posicaoAndar(limbSwing), limbSwingAmount, 2.0F, 2.5F);
        sentar.estado(entidade.sitAnimationState, ageInTicks);
        poseSentado.estado(entidade.sitPoseAnimationState, ageInTicks);
        levantar.estado(entidade.sitUpAnimationState, ageInTicks);
        ocioso.estado(entidade.idleAnimationState, ageInTicks);
        disparar.estado(entidade.dashAnimationState, ageInTicks);
    }
}
