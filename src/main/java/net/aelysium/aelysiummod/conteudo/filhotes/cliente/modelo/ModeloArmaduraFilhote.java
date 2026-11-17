package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import com.google.common.collect.Iterables;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class ModeloArmaduraFilhote<T extends LivingEntity> extends HumanoidModel<T> {

    private final ModelPart cintura;

    public ModeloArmaduraFilhote(ModelPart raiz) {
        super(raiz);
        this.cintura = raiz.getChild("waist");
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), List.of(cintura));
    }

    public void sincronizar(HumanoidModel<T> pai, float desvioBraco) {
        pai.copyPropertiesTo(this);
        cintura.resetPose();
        cintura.xRot = pai.body.xRot;
        cintura.yRot = pai.body.yRot;
        cintura.zRot = pai.body.zRot;
        rightArm.x -= desvioBraco;
        leftArm.x += desvioBraco;
        young = false;
    }
}
