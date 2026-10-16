package net.aelysium.aelysiummod.nucleo.cliente;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BotaoExcluir {

    private static final ResourceLocation NORMAL =
            ResourceLocation.fromNamespaceAndPath("aelysiummod", "textures/gui/botao_excluir.png");
    private static final ResourceLocation HOVER =
            ResourceLocation.fromNamespaceAndPath("aelysiummod", "textures/gui/botao_excluir_hover.png");
    private static final int TAMANHO = 9;

    private BotaoExcluir() {}

    public static void desenhar(GuiGraphics g, int x, int y, int largura, int altura, boolean hover) {
        int escala = Math.max(1, Math.min(largura, altura) / TAMANHO);
        int lado = TAMANHO * escala;
        int px = x + (largura - lado) / 2;
        int py = y + (altura - lado) / 2;
        g.pose().pushPose();
        g.pose().translate(px, py, 0);
        g.pose().scale(escala, escala, 1f);
        g.blit(hover ? HOVER : NORMAL, 0, 0, 0, 0, TAMANHO, TAMANHO, TAMANHO, TAMANHO);
        g.pose().popPose();
    }
}
