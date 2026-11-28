package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ia.animacao;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.base.MowzieEntity;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.animacao.Animation;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.animacao.IAnimatedEntity;

import java.util.EnumSet;

public class SimpleAnimationAI<T extends MowzieEntity & IAnimatedEntity> extends AnimationAI<T> {
    protected final Animation animation;

    public SimpleAnimationAI(T entity, Animation animation) {
        super(entity);
        this.animation = animation;
    }

    public SimpleAnimationAI(T entity, Animation animation, boolean interruptsAI) {
        super(entity, interruptsAI);
        this.animation = animation;
    }

    public SimpleAnimationAI(T entity, Animation animation, boolean interruptsAI, boolean hurtInterruptsAnimation) {
        super(entity, interruptsAI, hurtInterruptsAnimation);
        this.animation = animation;
    }

    public SimpleAnimationAI(T entity, Animation animation, EnumSet<Flag> interruptFlagTypes) {
        super(entity);
        this.animation = animation;
        setFlags(interruptFlagTypes);
    }

    @Override
    protected boolean test(Animation animation) {
        return animation == this.animation;
    }
}
