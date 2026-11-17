package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.aelysium.aelysiummod.conteudo.filhotes.cliente.EstadosAnimacaoFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.animacao.BabyAxolotlAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

public class ModeloAxolotlFilhote extends ModeloFilhoteHierarquico<Axolotl> {

    private final AnimacaoFilhote andar;
    private final AnimacaoFilhote andarSubmerso;
    private final AnimacaoFilhote nadar;
    private final AnimacaoFilhote paradoNoChao;
    private final AnimacaoFilhote paradoSubmersoNoChao;
    private final AnimacaoFilhote paradoSubmerso;
    private final AnimacaoFilhote fingirMorto;

    public ModeloAxolotlFilhote(ModelPart raiz) {
        super(raiz);
        this.nadar = AnimacaoFilhote.preparar(raiz, BabyAxolotlAnimation.BABY_AXOLOTL_SWIM);
        this.andar = AnimacaoFilhote.preparar(raiz, BabyAxolotlAnimation.AXOLOTL_WALK_FLOOR);
        this.andarSubmerso = AnimacaoFilhote.preparar(raiz, BabyAxolotlAnimation.WALK_FLOOR_UNDERWATER);
        this.paradoSubmerso = AnimacaoFilhote.preparar(raiz, BabyAxolotlAnimation.IDLE_UNDERWATER);
        this.paradoSubmersoNoChao = AnimacaoFilhote.preparar(raiz, BabyAxolotlAnimation.IDLE_FLOOR_UNDERWATER);
        this.paradoNoChao = AnimacaoFilhote.preparar(raiz, BabyAxolotlAnimation.BABY_AXOLOTL_IDLE_FLOOR);
        this.fingirMorto = AnimacaoFilhote.preparar(raiz, BabyAxolotlAnimation.BABY_AXOLOTL_PLAY_DEAD);
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partdefinition = mesh.getRoot();
        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -0.75F, -2.75F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 12).addBox(0.0F, -1.75F, -2.75F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.25F, 1.75F));
        body.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(20, 16).addBox(-3.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 0.25F, -1.25F));
        PartDefinition rightLeg = body.addOrReplaceChild("right_hind_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 0.25F, 1.75F, 0.0F, 1.5708F, 1.5708F));
        rightLeg.addOrReplaceChild("right_leg_r1", CubeListBuilder.create().texOffs(20, 14).addBox(0.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));
        body.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(20, 13).addBox(0.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 0.25F, -1.25F));
        body.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(20, 14).addBox(0.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 0.25F, 1.75F));
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(10, 9).addBox(0.0F, -1.5F, -1.0F, 0.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.25F, 3.25F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 8).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.25F, -2.75F));
        head.addOrReplaceChild("left_gills", CubeListBuilder.create().texOffs(20, 8).addBox(0.0F, -3.5F, 0.0F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -0.5F, -2.0F));
        head.addOrReplaceChild("right_gills", CubeListBuilder.create().texOffs(20, 3).addBox(-3.0F, -3.5F, 0.0F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -0.5F, -2.0F));
        head.addOrReplaceChild("top_gills", CubeListBuilder.create().texOffs(20, 0).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -2.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(Axolotl entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        UtilFilhotes.resetar(raiz);
        EstadosAnimacaoFilhotes.Axolote estado = EstadosAnimacaoFilhotes.axolote(entidade);
        if (estado.andar.isStarted()) {
            andar.andar(UtilFilhotes.posicaoAndar(limbSwing), limbSwingAmount, 15.0F, 30.0F);
        }
        nadar.estado(estado.nadar, ageInTicks);
        andarSubmerso.estado(estado.andarSubmerso, ageInTicks);
        paradoNoChao.estado(estado.paradoNoChao, ageInTicks);
        paradoSubmerso.estado(estado.paradoSubmerso, ageInTicks);
        paradoSubmersoNoChao.estado(estado.paradoSubmersoNoChao, ageInTicks);
        fingirMorto.estado(estado.fingirMorto, ageInTicks);
    }
}
