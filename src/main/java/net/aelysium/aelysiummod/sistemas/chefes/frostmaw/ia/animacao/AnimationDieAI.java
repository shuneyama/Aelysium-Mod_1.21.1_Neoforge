package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ia.animacao;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.base.MowzieLLibraryEntity;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.animacao.IAnimatedEntity;

public class AnimationDieAI<T extends MowzieLLibraryEntity & IAnimatedEntity> extends SimpleAnimationAI<T> {
    public AnimationDieAI(T entity) {
        super(entity, entity.getDeathAnimation(), false);
    }
}
