package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.aelysium.aelysiummod.conteudo.filhotes.cliente.EstadosAnimacaoFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.animacao.AdultRabbitAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.Rabbit;

public class ModeloCoelhoAdulto extends ModeloFilhoteHierarquico<Rabbit> {

    private final ModelPart cabeca;
    private final AnimacaoFilhote pulo;
    private final AnimacaoFilhote inclinarCabeca;

    public ModeloCoelhoAdulto(ModelPart raiz) {
        super(raiz);
        this.cabeca = raiz.getChild("body").getChild("head");
        this.pulo = AnimacaoFilhote.preparar(raiz, AdultRabbitAnimation.HOP);
        this.inclinarCabeca = AnimacaoFilhote.preparar(raiz, AdultRabbitAnimation.IDLE_HEAD_TILT);
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -6.0F, -9.0F, 8.0F, 6.0F, 10.0F), PartPose.offsetAndRotation(0.0F, 23.0F, 4.0F, -0.3927F, 0.0F, 0.0F));
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(20, 16).addBox(-2.0F, -3.0084F, -1.0125F, 4.0F, 4.0F, 4.0F), PartPose.offset(0.0F, -4.9916F, 0.0125F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16).addBox(-2.5F, -3.0F, -4.0F, 5.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, -5.2929F, -8.1213F, 0.3927F, 0.0F, 0.0F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -4.2929F, -0.1213F, 2.0F, 5.0F, 1.0F), PartPose.offset(1.5F, -3.7071F, -0.8787F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, -4.2929F, -0.1213F, 2.0F, 5.0F, 1.0F), PartPose.offset(-1.5F, -3.7071F, -0.8787F));
        PartDefinition frontLegs = body.addOrReplaceChild("frontlegs", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5349F, -6.3108F));
        frontLegs.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(36, 18).addBox(-0.9F, -1.0F, -0.9F, 2.0F, 4.0F, 2.0F), PartPose.offsetAndRotation(-2.0F, 1.9239F, 0.3827F, 0.3927F, 0.0F, 0.0F));
        frontLegs.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(44, 18).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 4.0F, 2.0F), PartPose.offsetAndRotation(2.0F, 1.9239F, 0.4827F, 0.3927F, 0.0F, 0.0F));
        PartDefinition backLegs = root.addOrReplaceChild("backlegs", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 4.0F));
        PartDefinition rightBackLeg = backLegs.addOrReplaceChild("right_hind_leg", CubeListBuilder.create(), PartPose.offset(-3.0F, 0.5F, 0.0F));
        rightBackLeg.addOrReplaceChild("right_haunch", CubeListBuilder.create().texOffs(20, 24).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 1.0F, 6.0F), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, 0.3927F, 0.0F));
        PartDefinition leftBackLeg = backLegs.addOrReplaceChild("left_hind_leg", CubeListBuilder.create(), PartPose.offset(3.0F, 0.5F, 0.0F));
        leftBackLeg.addOrReplaceChild("left_haunch", CubeListBuilder.create().texOffs(36, 24).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 1.0F, 6.0F), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, -0.3927F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(Rabbit entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        UtilFilhotes.resetar(raiz);
        EstadosAnimacaoFilhotes.Coelho estado = EstadosAnimacaoFilhotes.coelho(entidade);
        if (!estado.inclinarCabeca.isStarted()) {
            UtilFilhotes.cabeca(cabeca, headPitch, netHeadYaw);
        }
        pulo.estado(estado.pulo, ageInTicks);
        inclinarCabeca.estado(estado.inclinarCabeca, ageInTicks);
    }
}
