package net.aelysium.aelysiummod.client;

import net.aelysium.aelysiummod.lua.TipoLua;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@OnlyIn(Dist.CLIENT)
public class LuaClientRenderer {

    public static TipoLua luaCliente = TipoLua.NORMAL;

    public static float transitionProgress = 0.0f;

    private static final float TRANSITION_SPEED = 0.0025f;

    public static final float NORMAL_SIZE = 20.0f;

    public static final float BLOOD_MOON_SIZE = 100.0f;

    public static void updateTransition() {
        if (luaCliente == TipoLua.VERMELHA) {
            if (transitionProgress < 1.0f) {
                transitionProgress += TRANSITION_SPEED;
                if (transitionProgress > 1.0f) {
                    transitionProgress = 1.0f;
                }
            }
        } else {
            if (transitionProgress > 0.0f) {
                transitionProgress -= TRANSITION_SPEED;
                if (transitionProgress < 0.0f) {
                    transitionProgress = 0.0f;
                }
            }
        }
    }

    public static float getCurrentSize() {
        float smooth = smoothstep(transitionProgress);
        return NORMAL_SIZE + (BLOOD_MOON_SIZE - NORMAL_SIZE) * smooth;
    }

    public static float[] getCorLua() {
        float smooth = smoothstep(transitionProgress);
        float r = 1.0f;
        float g = 1.0f - smooth;
        float b = 1.0f - smooth;
        return new float[]{r, g, b};
    }

    public static float getBloodMoonOpacity() {
        return smoothstep(transitionProgress);
    }

    private static float smoothstep(float t) {
        return t * t * (3.0f - 2.0f * t);
    }

    @SubscribeEvent
    public void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;

        if (level == null) return;
        if (transitionProgress <= 0.0f) return;

        long tempo = level.getDayTime() % 24000;
        if (tempo < 13000 || tempo > 23000) return;

        float intensidade = calcularIntensidadeNoite(tempo);
        float fogStrength = smoothstep(transitionProgress) * intensidade * 0.6f;

        float r = lerp((float) event.getRed(), 0.15f, fogStrength);
        float g = lerp((float) event.getGreen(), 0.02f, fogStrength);
        float b = lerp((float) event.getBlue(), 0.02f, fogStrength);

        event.setRed(r);
        event.setGreen(g);
        event.setBlue(b);
    }

    private float calcularIntensidadeNoite(long tempo) {
        if (tempo >= 13000 && tempo <= 14000) {
            return (tempo - 13000) / 1000.0f;
        } else if (tempo >= 22000 && tempo <= 23000) {
            return 1.0f - ((tempo - 22000) / 1000.0f);
        } else if (tempo > 14000 && tempo < 22000) {
            return 1.0f;
        }
        return 0.0f;
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}