package net.aelysium.aelysiummod.eventos;

public class LuaEstado {
    public static boolean bloodMoon = false;

    public static float transitionProgress = 0.0F;

    public static final float TRANSITION_SPEED = 0.0005F;

    public static final float NORMAL_SIZE = 20.0F;
    public static final float BLOOD_MOON_SIZE = 100.0F;

    public static void updateTransition() {
        if (bloodMoon) {
            if (transitionProgress < 1.0F) {
                transitionProgress += TRANSITION_SPEED;
                if (transitionProgress > 1.0F) {
                    transitionProgress = 1.0F;
                }
            }
        } else {
            if (transitionProgress > 0.0F) {
                transitionProgress -= TRANSITION_SPEED;
                if (transitionProgress < 0.0F) {
                    transitionProgress = 0.0F;
                }
            }
        }
    }

    public static float getCurrentSize() {
        float smoothProgress = smoothstep(transitionProgress);
        return NORMAL_SIZE + (BLOOD_MOON_SIZE - NORMAL_SIZE) * smoothProgress;
    }

    public static float getBloodMoonOpacity() {
        return smoothstep(transitionProgress);
    }

    private static float smoothstep(float t) {
        return t * t * (3.0F - 2.0F * t);
    }
}