package net.aelysium.aelysiummod.conteudo.item.escudo.cliente;

import net.aelysium.aelysiummod.conteudo.item.escudo.GerenciadorBarreira;
import net.aelysium.aelysiummod.conteudo.item.escudo.EscudoAbismoItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@OnlyIn(Dist.CLIENT)
public class RenderizadorTemporizadorBarreira {

    private static final int ALTURA_HOTBAR = 22;
    private static final int DISTANCIA_HOTBAR = 60;
    private static final float ESCALA = 1.5F;
    private static final int COR = 0x55FFFF;

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !mc.player.isUsingItem()) {
            return;
        }
        if (!(mc.player.getUseItem().getItem() instanceof EscudoAbismoItem)) {
            return;
        }

        int carga = mc.player.getPersistentData().getInt(GerenciadorBarreira.CHAVE_CARGA);
        if (carga <= 0 || carga >= GerenciadorBarreira.TICKS_CARGA) {
            return;
        }

        int segundos = (int) Math.ceil((GerenciadorBarreira.TICKS_CARGA - carga) / 20.0);
        String texto = segundos + "s";

        GuiGraphics graphics = event.getGuiGraphics();
        int x = mc.getWindow().getGuiScaledWidth() / 2;
        int y = mc.getWindow().getGuiScaledHeight() - ALTURA_HOTBAR - DISTANCIA_HOTBAR;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(ESCALA, ESCALA, 1.0F);
        graphics.drawCenteredString(mc.font, texto, 0, 0, COR);
        graphics.pose().popPose();
    }
}
