package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.animacao;

public class Animation {
    @Deprecated
    private int id;
    private final int duration;
    private boolean looping;

    protected Animation(int duration) {
        this.duration = duration;
    }

    @Deprecated
    public static Animation create(int id, int duration) {
        Animation animation = Animation.create(duration);
        animation.id = id;
        return animation;
    }

    public static Animation create(int duration) {
        return new Animation(duration);
    }

    @Deprecated
    public int getID() {
        return this.id;
    }

    public int getDuration() {
        return this.duration;
    }

    public Animation setLooping(boolean loops) {
        looping = loops;
        return this;
    }

    public boolean doesLoop() {
        return looping;
    }
}
