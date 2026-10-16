package net.aelysium.aelysiummod.sistemas.rp.raca.cliente;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;

public class RenderizadorFogoRaca {

    private static volatile boolean imuneFogo = false;

    public static void definir(boolean ativa) {
        imuneFogo = ativa;
    }

    @SubscribeEvent
    public void aoRenderizarEfeitoDeTela(RenderBlockScreenEffectEvent event) {
        if (!imuneFogo || event.getOverlayType() != RenderBlockScreenEffectEvent.OverlayType.FIRE) return;
        var nivel = Minecraft.getInstance().level;
        if (nivel != null && nivel.dimension() == Level.OVERWORLD) event.setCanceled(true);
    }

    @SubscribeEvent
    public void aoSair(ClientPlayerNetworkEvent.LoggingOut event) {
        imuneFogo = false;
    }
}
