package net.aelysium.aelysiummod.nucleo.texto;

import java.awt.Color;

public final class EfeitosTexto {

    public static final int RAINBOW = 0;
    public static final int GLITCH = 1;
    public static final int WAVE = 2;
    public static final int SHAKE = 3;
    public static final int PULSE = 4;
    public static final int NEON = 5;
    public static final int BOUNCE = 6;
    public static final int GLOW = 7;
    public static final int VIBRATE = 8;
    public static final int WIGGLE = 9;

    private EfeitosTexto() {}

    public static double segundos() {
        return System.nanoTime() / 1.0E9;
    }

    public static float ruido(long semente) {
        long h = semente * -7046029254386353131L;
        h ^= h >>> 32;
        h *= -7046029254386353131L;
        h ^= h >>> 29;
        return ((h & 0xFFFFL) / 65535.0f) * 2.0f - 1.0f;
    }

    public static boolean usaCor(int efeito) {
        return efeito == RAINBOW || efeito == PULSE || efeito == NEON || efeito == GLOW;
    }

    public static int cor(int efeito, double t, float x, int base) {
        switch (efeito) {
            case RAINBOW -> {
                float matiz = (float) ((t * 0.4 - x * 0.012) % 1.0);
                if (matiz < 0) matiz += 1.0f;
                return Color.HSBtoRGB(matiz, 0.75f, 1.0f) & 0xFFFFFF;
            }
            case NEON -> {
                float matiz = 0.5f + 0.35f * (0.5f + 0.5f * (float) Math.sin(t * 3.0 - x * 0.02));
                return Color.HSBtoRGB(matiz, 0.9f, 1.0f) & 0xFFFFFF;
            }
            case PULSE -> {
                float fator = 0.55f + 0.45f * (0.5f + 0.5f * (float) Math.sin(t * 5.0));
                return misturar(base, 0x000000, 1.0f - fator);
            }
            case GLOW -> {
                float fator = 0.5f + 0.5f * (float) Math.sin(t * 3.0);
                return misturar(base, 0xFFFFFF, 0.55f * fator);
            }
            default -> {
                return base;
            }
        }
    }

    private static int misturar(int a, int b, float fator) {
        int r = Math.round(((a >> 16) & 255) * (1 - fator) + ((b >> 16) & 255) * fator);
        int g = Math.round(((a >> 8) & 255) * (1 - fator) + ((b >> 8) & 255) * fator);
        int bl = Math.round((a & 255) * (1 - fator) + (b & 255) * fator);
        return (r << 16) | (g << 8) | bl;
    }

    public static float deslocamentoX(int efeito, double t, float x) {
        switch (efeito) {
            case SHAKE -> {
                return ruido((long) (t * 30.0) * 31L + (long) x) * 0.8f;
            }
            case VIBRATE -> {
                return (float) Math.sin(t * 80.0 + x) * 0.4f;
            }
            case WIGGLE -> {
                return (float) Math.sin(t * 8.0 - x * 0.3) * 1.0f;
            }
            case GLITCH -> {
                long quadro = (long) (t * 12.0);
                float sorteio = ruido(quadro * 97L + (long) (x / 6.0f));
                return sorteio > 0.7f ? ruido(quadro * 13L + (long) x) * 2.5f : 0.0f;
            }
            default -> {
                return 0.0f;
            }
        }
    }

    public static float deslocamentoY(int efeito, double t, float x) {
        switch (efeito) {
            case WAVE -> {
                return (float) Math.sin(t * 6.0 - x * 0.35) * 1.5f;
            }
            case BOUNCE -> {
                return -Math.abs((float) Math.sin(t * 5.0 - x * 0.2)) * 2.5f;
            }
            case SHAKE -> {
                return ruido((long) (t * 30.0) * 17L + (long) x + 5L) * 0.8f;
            }
            case WIGGLE -> {
                return (float) Math.cos(t * 8.0 - x * 0.3) * 0.5f;
            }
            case GLITCH -> {
                long quadro = (long) (t * 12.0);
                float sorteio = ruido(quadro * 53L + (long) (x / 6.0f));
                return sorteio > 0.85f ? ruido(quadro * 7L + (long) x) * 1.5f : 0.0f;
            }
            default -> {
                return 0.0f;
            }
        }
    }
}
