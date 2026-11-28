package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ia.animacao;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.base.MowzieEntity;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.animacao.Animation;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.animacao.IAnimatedEntity;

public class AnimationDeactivateAI<T extends MowzieEntity & IAnimatedEntity> extends SimpleAnimationAI<T> {
    public AnimationDeactivateAI(T entity, Animation animation) {
        super(entity, animation);
    }

    @Override
    public void stop() {
        super.stop();
        entity.active = false;
    }
}
