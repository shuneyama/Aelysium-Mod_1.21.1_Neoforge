package net.aelysium.aelysiummod.eventos;

public class LuaEstado {
    public static boolean bloodMoon = false;
    public static float transitionProgress = 0.0F;

    public static final float TRANSITION_SPEED = 0.0005F;
    public static final float NORMAL_SIZE = 20.0F;
    public static final float BLOOD_MOON_SIZE = 100.0F;

    /**
     * Atualiza a transição da lua vermelha.
     * Deve ser chamado a cada tick no cliente.
     */
    public static void updateTransition() {
        if (bloodMoon) {
            // Transição para lua vermelha
            if (transitionProgress < 1.0F) {
                transitionProgress += TRANSITION_SPEED;
                if (transitionProgress > 1.0F) {
                    transitionProgress = 1.0F;
                }
            }
        } else {
            // Transição para lua normal
            if (transitionProgress > 0.0F) {
                transitionProgress -= TRANSITION_SPEED;
                if (transitionProgress < 0.0F) {
                    transitionProgress = 0.0F;
                }
            }
        }
    }

    /**
     * Retorna o tamanho atual da lua baseado na transição.
     */
    public static float getCurrentSize() {
        float smoothProgress = smoothstep(transitionProgress);
        return NORMAL_SIZE + (BLOOD_MOON_SIZE - NORMAL_SIZE) * smoothProgress;
    }

    /**
     * Retorna a opacidade da cor vermelha da lua.
     */
    public static float getBloodMoonOpacity() {
        return smoothstep(transitionProgress);
    }

    /**
     * Função smoothstep para transições suaves.
     * Retorna valor entre 0 e 1 com aceleração/desaceleração suave.
     */
    private static float smoothstep(float t) {
        return t * t * (3.0F - 2.0F * t);
    }

    /**
     * Reseta o estado da lua para o padrão.
     */
    public static void reset() {
        bloodMoon = false;
        transitionProgress = 0.0F;
    }
}