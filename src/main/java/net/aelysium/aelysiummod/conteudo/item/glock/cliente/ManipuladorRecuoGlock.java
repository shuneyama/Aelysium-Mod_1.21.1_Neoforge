package net.aelysium.aelysiummod.conteudo.item.glock.cliente;

import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@OnlyIn(Dist.CLIENT)
public class ManipuladorRecuoGlock {

    private static final float RECUO_POR_TIRO = 3.0f;
    private static final float RECUO_MAXIMO = 12.0f;
    private static final float FATOR_RECUPERACAO = 0.68f;
    private static final float LIMIAR_ZERO = 0.02f;

    private static float recuoAnteriorTick = 0f;
    private static float recuoAtual = 0f;

    public static void aplicarRecuo() {
        recuoAtual = Math.min(RECUO_MAXIMO, recuoAtual + RECUO_POR_TIRO);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        recuoAnteriorTick = recuoAtual;

        if (recuoAtual <= 0f) return;

        recuoAtual *= FATOR_RECUPERACAO;
        if (recuoAtual < LIMIAR_ZERO) {
            recuoAtual = 0f;
        }
    }

    @SubscribeEvent
    public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float recuo = Mth.lerp((float) event.getPartialTick(), recuoAnteriorTick, recuoAtual);
        if (recuo <= 0f) return;
        event.setPitch(event.getPitch() - recuo);
    }
}
