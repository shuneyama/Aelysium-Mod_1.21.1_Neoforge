package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;

public final class MalhasMonstrosFilhotes {

    private MalhasMonstrosFilhotes() {
    }

    public static LayerDefinition zumbi(CubeDeformation g) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-2.0F, -2.5F, -1.0F, 4.0F, 5.0F, 2.0F, g), PartPose.offset(0.0F, 17.5F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(3, 3).addBox(-3.0F, -6.25F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(35, 3).addBox(-3.0F, -6.15F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 15.25F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(36, 16).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, g), PartPose.offset(-3.0F, 15.5F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(28, 16).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, g), PartPose.offset(3.0F, 15.5F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(8, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, g), PartPose.offset(-1.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, g), PartPose.offset(1.0F, 20.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition aldeaoZumbi() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 15).addBox(-2.0F, -2.75F, -1.5F, 4.0F, 5.0F, 3.0F).texOffs(16, 22).addBox(-2.0F, -2.75F, -1.5F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 18.75F, 0.0F));
        PartDefinition cabeca = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -3.5F, 8.0F, 8.0F, 7.0F), PartPose.offset(0.0F, 16.0F, 0.0F));
        cabeca.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(23, 0).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -1.0F, -4.0F));
        PartDefinition chapeu = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 31).addBox(-4.0F, -8.0F, -3.5F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.3F)), PartPose.ZERO);
        chapeu.addOrReplaceChild("hat_rim", CubeListBuilder.create().texOffs(0, 46).addBox(-7.0F, -0.5F, -6.0F, 14.0F, 1.0F, 12.0F), PartPose.offset(0.0F, -4.5F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 15).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 15.5F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 15).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 15.5F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(8, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(-1.0F, 21.5F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(1.0F, 21.5F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition aldeao() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bracos = root.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offset(0.0F, 17.5F, 0.0F));
        bracos.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(36, 15).addBox(-1.0F, -2.4925F, -1.8401F, 2.0F, 4.0F, 2.0F).texOffs(16, 15).addBox(5.0F, -2.4925F, -1.8401F, 2.0F, 4.0F, 2.0F), PartPose.offsetAndRotation(-3.0F, 1.4025F, -0.9599F, -1.0472F, 0.0F, 0.0F));
        bracos.addOrReplaceChild("middlearm_r1", CubeListBuilder.create().texOffs(24, 17).addBox(-2.0F, -0.9924F, -0.9825F, 4.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.9024F, -1.8175F, -1.0472F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(8, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(-1.0F, 21.5F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(1.0F, 21.5F, 0.0F));
        PartDefinition cabeca = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -3.5F, 8.0F, 8.0F, 7.0F), PartPose.offset(0.0F, 16.0F, 0.0F));
        PartDefinition chapeu = cabeca.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 30).addBox(-4.0F, -4.0F, -3.5F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, -4.0F, 0.0F));
        chapeu.addOrReplaceChild("hat_rim", CubeListBuilder.create().texOffs(0, 45).addBox(-7.0F, -0.5F, -6.0F, 14.0F, 1.0F, 12.0F), PartPose.offset(0.0F, -0.5F, 0.0F));
        cabeca.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(23, 0).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -2.0F, -4.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 15).addBox(-2.0F, -2.75F, -1.5F, 4.0F, 5.0F, 3.0F), PartPose.offset(0.0F, 18.75F, 0.0F));
        root.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(16, 21).addBox(-2.5F, -8.0F, -1.5F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.2F)), PartPose.offset(0.5F, 24.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition piglin() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 5.0F, 3.0F), PartPose.offset(0.0F, 18.0F, -0.5F));
        PartDefinition cabeca = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(21, 30).addBox(-1.5F, -3.0F, -4.5F, 3.0F, 3.0F, 1.0F).texOffs(0, 0).addBox(-4.5F, -6.0F, -3.5F, 9.0F, 6.0F, 7.0F), PartPose.offset(0.0F, 15.0F, 0.0F));
        PartDefinition orelhaEsquerda = cabeca.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(4.2F, -4.0F, 0.0F));
        orelhaEsquerda.addOrReplaceChild("left_ear_r1", CubeListBuilder.create().texOffs(0, 21).addBox(-0.5F, -3.0F, -2.0F, 1.0F, 6.0F, 4.0F), PartPose.offsetAndRotation(1.0F, 1.75F, 0.0F, 0.0F, 0.0F, -0.6109F));
        PartDefinition orelhaDireita = cabeca.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-4.2F, -4.0F, 0.0F));
        orelhaDireita.addOrReplaceChild("right_ear_r1", CubeListBuilder.create().texOffs(18, 13).addBox(-0.5F, -3.0F, -2.0F, 1.0F, 6.0F, 4.0F), PartPose.offsetAndRotation(-1.0F, 1.75F, 0.0F, 0.0F, 0.0F, 0.6109F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(28, 13).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F), PartPose.offset(4.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(10, 30).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F), PartPose.offset(-4.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(22, 23).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(-1.5F, 20.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(10, 23).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(1.5F, 20.0F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("ear", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("cloak", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_sleeve", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_sleeve", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_pants", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_pants", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("jacket", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition hoglin() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition cabeca = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -2.2605F, -10.547F, 10.0F, 4.0F, 12.0F).texOffs(44, 29).addBox(-7.0F, -4.0981F, -8.4879F, 2.0F, 5.0F, 2.0F).texOffs(52, 29).addBox(5.0F, -4.0981F, -8.4879F, 2.0F, 5.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 13.0F, -7.0F, 0.8727F, 0.0F, 0.0F));
        PartDefinition corpo = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -14.0F, -7.0F, 8.0F, 8.0F, 14.0F, new CubeDeformation(0.02F)).texOffs(24, 39).addBox(0.0F, -18.0F, -8.0F, 0.0F, 6.0F, 11.0F, new CubeDeformation(0.02F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        corpo.addOrReplaceChild("mane", CubeListBuilder.create(), PartPose.ZERO);
        cabeca.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(32, 5).addBox(-5.1F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(-5.0F, -1.0F, -1.5F, 0.0F, 0.0F, -0.8727F));
        cabeca.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-0.9F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F).mirror(false), PartPose.offsetAndRotation(5.0F, -1.0F, -1.5F, 0.0F, 0.0F, 0.8727F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 47).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(-2.5F, 18.0F, 4.5F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(12, 47).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(2.5F, 18.0F, 4.5F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(-2.5F, 18.0F, -4.5F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(12, 38).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(2.5F, 18.0F, -4.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition armadura(EquipmentSlot parte, CubeDeformation g, float desvioX, float desvioY, float desvioZ) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        boolean cabecaAtiva = parte == EquipmentSlot.HEAD;
        boolean peito = parte == EquipmentSlot.CHEST;
        boolean pernas = parte == EquipmentSlot.LEGS;
        boolean pes = parte == EquipmentSlot.FEET;
        CubeListBuilder vazio = CubeListBuilder.create();
        root.addOrReplaceChild("head", cabecaAtiva ? CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -7.0F, -4.5F, 9.0F, 8.0F, 8.0F, g) : vazio, PartPose.offset(0.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", peito ? CubeListBuilder.create().texOffs(0, 17).addBox(-3.0F, -3.0F, -1.5F, 6.0F, 5.0F, 3.0F, g) : CubeListBuilder.create(), PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("waist", pernas ? CubeListBuilder.create().texOffs(0, 36).addBox(-3.0F, -1.2F, -1.49F, 5.9F, 2.0F, 2.9F, g.extend(-0.1F)) : CubeListBuilder.create(), PartPose.offset(0.0F, 19.0F, 0.0F));
        root.addOrReplaceChild("right_arm", peito ? CubeListBuilder.create().texOffs(30, 25).addBox(-1.0F, 0.0F, -1.53F, 2.0F, 5.0F, 3.0F, g) : CubeListBuilder.create(), PartPose.offset(-3.5F - desvioX, 15.5F + desvioY, desvioZ));
        root.addOrReplaceChild("left_arm", peito ? CubeListBuilder.create().texOffs(30, 17).addBox(-1.0F, 0.0F, -1.53F, 2.0F, 5.0F, 3.0F, g) : CubeListBuilder.create(), PartPose.offset(3.5F + desvioX, 15.5F + desvioY, desvioZ));
        PartDefinition pernaEsquerda = root.addOrReplaceChild("left_leg", pernas ? CubeListBuilder.create().texOffs(18, 24).addBox(-2.0F, -0.2F, -2.0F, 3.0F, 4.0F, 3.0F, g.extend(-0.1F)) : CubeListBuilder.create(), PartPose.offset(1.5F, 20.0F, 0.5F));
        PartDefinition pernaDireita = root.addOrReplaceChild("right_leg", pernas ? CubeListBuilder.create().texOffs(18, 17).addBox(-1.0F, -0.2F, -2.0F, 3.0F, 4.0F, 3.0F, g.extend(-0.1F)) : CubeListBuilder.create(), PartPose.offset(-1.5F, 20.0F, 0.5F));
        pernaEsquerda.addOrReplaceChild("right_foot", pes ? CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, 2.9F, -2.0F, 3.0F, 1.0F, 3.0F, g) : CubeListBuilder.create(), PartPose.ZERO);
        pernaDireita.addOrReplaceChild("left_foot", pes ? CubeListBuilder.create().texOffs(0, 29).mirror().addBox(-1.0F, 2.9F, -2.0F, 3.0F, 1.0F, 3.0F, g).mirror(false) : CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }
}
