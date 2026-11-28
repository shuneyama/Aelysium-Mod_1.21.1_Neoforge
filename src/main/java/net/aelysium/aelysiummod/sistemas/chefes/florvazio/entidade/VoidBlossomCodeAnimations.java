package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.ModeloBedrock;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.animacao.ICodeAnimations;
import net.minecraft.util.Mth;

public class VoidBlossomCodeAnimations implements ICodeAnimations<VoidBlossomEntity> {
    @Override
    public void animate(VoidBlossomEntity animatable, ModeloBedrock model, float partialTick) {
        float bodyYaw = Mth.rotLerp(partialTick, animatable.yBodyRotO, animatable.yBodyRot);

        model.getBone("Leaves").ifPresent(bone -> bone.setRotY((float) Math.toRadians(bodyYaw)));
        model.getBone("Thorns").ifPresent(bone -> bone.setRotY((float) Math.toRadians(bodyYaw)));
        model.getBone("Roots").ifPresent(bone -> bone.setRotY((float) Math.toRadians(bodyYaw)));
    }
}
