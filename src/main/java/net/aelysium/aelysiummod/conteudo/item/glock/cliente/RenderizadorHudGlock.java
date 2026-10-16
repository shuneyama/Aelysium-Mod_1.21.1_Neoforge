package net.aelysium.aelysiummod.conteudo.item.glock.cliente;

import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.aelysium.aelysiummod.conteudo.item.glock.GlockItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@OnlyIn(Dist.CLIENT)
public class RenderizadorHudGlock {

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        ItemStack mao = mc.player.getMainHandItem();
        if (!(mao.getItem() instanceof GlockItem)) {
            mao = mc.player.getOffhandItem();
            if (!(mao.getItem() instanceof GlockItem)) return;
        }

        int municao = mao.getOrDefault(ModComponentesDados.GLOCK_MUNICAO, 0);
        String texto = municao + "/" + GlockItem.CAPACIDADE_MAGAZINE;

        GuiGraphics gfx = event.getGuiGraphics();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int rightEdge = screenW / 2 + 91;
        int x = rightEdge + 4;
        int y = screenH - 33 - mc.font.lineHeight + 3;

        int cor = municao <= 0 ? 0xFFFF4444 : 0xFFFFFFFF;
        gfx.drawString(mc.font, texto, x, y, cor, true);
    }
}
