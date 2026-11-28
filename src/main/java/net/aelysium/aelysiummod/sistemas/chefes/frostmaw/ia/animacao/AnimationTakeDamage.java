package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ia.animacao;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.base.MowzieLLibraryEntity;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.animacao.IAnimatedEntity;

import java.util.EnumSet;

public class AnimationTakeDamage<T extends MowzieLLibraryEntity & IAnimatedEntity> extends SimpleAnimationAI<T> {
    public AnimationTakeDamage(T entity) {
        super(entity, entity.getHurtAnimation());
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }
}
