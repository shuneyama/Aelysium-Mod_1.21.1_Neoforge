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

public class ModeloCavaloFilhote<T extends AbstractHorse> extends ModeloEquinoFilhote<T> {

    public ModeloCavaloFilhote(ModelPart raiz) {
        super(raiz, raiz.getChild("head_parts"), raiz.getChild("right_hind_leg"), raiz.getChild("right_front_leg"), raiz.getChild("left_hind_leg"), raiz.getChild("left_front_leg"), raiz.getChild("body").getChild("tail"));
    }

    public static LayerDefinition criarCamada() {
        CubeDeformation g = CubeDeformation.NONE;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 13).addBox(-4.0F, -3.5F, -7.0F, 8.0F, 7.0F, 14.0F, g), PartPose.offset(0.0F, 12.5F, 0.0F));
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(24, 34).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 8.0F, g), PartPose.offsetAndRotation(0.0F, -1.0F, 7.0F, -0.7418F, 0.0F, 0.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(12, 46).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(2.4F, 16.0F, 5.4F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 46).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(-2.4F, 16.0F, 5.4F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(2.4F, 16.0F, -5.4F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(-2.4F, 16.0F, -5.4F));
        PartDefinition neck = root.addOrReplaceChild("head_parts", CubeListBuilder.create().texOffs(30, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 8.0F, 4.0F, g), PartPose.offsetAndRotation(0.0F, 10.0F, -6.0F, 0.6109F, 0.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.9484F, -6.705F, 6.0F, 4.0F, 9.0F, g), PartPose.offset(0.0F, -6.0516F, -0.2951F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -2.5F, -0.8F, 2.0F, 3.0F, 1.0F, g), PartPose.offsetAndRotation(2.0F, -4.2484F, 1.9451F, 0.0F, 0.0F, 0.2618F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 3.0F, 1.0F, g), PartPose.offsetAndRotation(-2.0F, -4.2484F, 1.645F, 0.0F, 0.0F, -0.2618F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    protected float deslocamentoYEmpinado() {
        return 4.0F;
    }

    @Override
    protected float deslocamentoZEmpinado() {
        return 0.0F;
    }

    @Override
    protected float rotacaoCauda() {
        return -Mth.HALF_PI;
    }

    @Override
    protected void posicionarCabeca(float comer, float empinar) {
        partesCabeca.y += Mth.lerp(comer, Mth.lerp(empinar, 0.0F, -2.0F), 2.0F);
        partesCabeca.z = Mth.lerp(empinar, partesCabeca.z, -4.0F);
    }
}
