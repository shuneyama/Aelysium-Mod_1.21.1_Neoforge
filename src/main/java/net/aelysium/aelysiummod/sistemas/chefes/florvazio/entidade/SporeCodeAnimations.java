package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.ModeloBedrock;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.animacao.ICodeAnimations;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.projetil.SporeBallProjectile;
import net.minecraft.util.Mth;

public class SporeCodeAnimations implements ICodeAnimations<SporeBallProjectile> {
    @Override
    public void animate(SporeBallProjectile animatable, ModeloBedrock model, float partialTick) {
        float pitch = animatable.impacted ? animatable.getXRot() : Mth.rotLerp(partialTick, animatable.getXRot() - 5, animatable.getXRot());

        model.getBone("root1").ifPresent(bone -> bone.setRotX((float) Math.toRadians(pitch)));
    }
}
