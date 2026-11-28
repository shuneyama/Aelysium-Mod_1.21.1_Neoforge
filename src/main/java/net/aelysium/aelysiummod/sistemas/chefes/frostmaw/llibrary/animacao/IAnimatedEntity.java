package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.animacao;

public interface IAnimatedEntity {
    Animation NO_ANIMATION = Animation.create(0);

    int getAnimationTick();

    void setAnimationTick(int tick);

    Animation getAnimation();

    void setAnimation(Animation animation);

    Animation[] getAnimations();
}
