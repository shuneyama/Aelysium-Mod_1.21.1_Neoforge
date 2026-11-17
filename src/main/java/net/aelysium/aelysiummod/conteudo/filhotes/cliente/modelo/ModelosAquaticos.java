package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class ModelosAquaticos {

    private ModelosAquaticos() {
    }

    public static LayerDefinition lula() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 13.0F, 0.0F));
        CubeListBuilder tentaculo = CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 6.0F, 2.0F);
        for (int i = 0; i < 8; i++) {
            double angulo = i * Math.PI * 2.0D / 8.0D;
            float x = (float) Math.cos(angulo) * 3.0F;
            float z = (float) Math.sin(angulo) * 3.0F;
            float rotacao = (float) (i * Math.PI * -2.0D / 8.0D + Math.PI / 2.0D);
            root.addOrReplaceChild("tentacle" + i, tentaculo, PartPose.offsetAndRotation(x, 18.5F, z, 0.0F, rotacao, 0.0F));
        }
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition golfinho() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation d = new CubeDeformation(0.0F);
        PartDefinition corpo = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(20, 0).addBox(-3.0F, -2.5F, -4.0F, 6.0F, 5.0F, 8.0F, d), PartPose.offset(0.0F, 21.5F, 0.0F));
        PartDefinition cabeca = corpo.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.5F, -4.0F, 6.0F, 5.0F, 4.0F, d), PartPose.offset(0.0F, 1.0F, -4.0F));
        cabeca.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 9).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, d), PartPose.offset(0.0F, 0.5F, -4.0F));
        corpo.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(34, 18).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 6.0F, d), PartPose.offsetAndRotation(1.8F, 0.85F, -2.6F, 0.8727F, 0.0F, 1.7017F));
        corpo.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(48, 18).mirror().addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 6.0F, d).mirror(false), PartPose.offsetAndRotation(-1.8F, 0.85F, -2.6F, 0.8727F, 0.0F, -1.7017F));
        PartDefinition cauda = corpo.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 13).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 7.0F, d), PartPose.offset(0.0F, 1.0F, 4.0F));
        cauda.addOrReplaceChild("tail_fin", CubeListBuilder.create().texOffs(22, 13).addBox(-4.0F, -0.5F, -1.0F, 8.0F, 1.0F, 4.0F, d), PartPose.offset(0.0F, 0.0F, 6.0F));
        corpo.addOrReplaceChild("back_fin", CubeListBuilder.create().texOffs(42, 0).addBox(-0.5F, -1.0F, 1.0F, 1.0F, 3.0F, 4.0F, d), PartPose.offsetAndRotation(0.0F, -1.0F, -2.7F, 0.8727F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
