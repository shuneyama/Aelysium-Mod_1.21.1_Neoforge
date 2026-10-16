package net.aelysium.aelysiummod.conteudo.item.arma.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.item.arma.UltimoSuspiroItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@OnlyIn(Dist.CLIENT)
public class RenderizadorHudSabre {

    private static final ResourceLocation CARGA_CHEIA = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/gui/sabre_carga_cheia.png");
    private static final ResourceLocation CARGA_VAZIA = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/gui/sabre_carga_vazia.png");

    private static final int TAMANHO_TEXTURA = 16;
    private static final int TAMANHO = 9;
    private static final int ESPACO = 1;
    private static final int LARGURA_HOTBAR_METADE = 91;
    private static final int ALTURA_HOTBAR_METADE = 11;

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) {
            return;
        }

        ItemStack stack = mc.player.getMainHandItem();
        if (!(stack.getItem() instanceof UltimoSuspiroItem)) {
            stack = mc.player.getOffhandItem();
            if (!(stack.getItem() instanceof UltimoSuspiroItem)) {
                return;
            }
        }

        long agora = mc.level.getGameTime();
        UltimoSuspiroItem.Estado estado = UltimoSuspiroItem.estado(stack, agora);

        float progresso = 0.0F;
        if (estado.recargaFim() > 0) {
            progresso = Mth.clamp(1.0F - (float) (estado.recargaFim() - agora) / UltimoSuspiroItem.TICKS_RECARGA, 0.0F, 1.0F);
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int larguraTotal = UltimoSuspiroItem.CARGAS_MAX * TAMANHO + (UltimoSuspiroItem.CARGAS_MAX - 1) * ESPACO;
        int bordaHotbar = mc.getWindow().getGuiScaledWidth() / 2 - LARGURA_HOTBAR_METADE;
        int x0 = bordaHotbar / 2 - larguraTotal / 2;
        int y0 = mc.getWindow().getGuiScaledHeight() - ALTURA_HOTBAR_METADE - TAMANHO / 2;

        for (int i = 0; i < UltimoSuspiroItem.CARGAS_MAX; i++) {
            int x = x0 + i * (TAMANHO + ESPACO);
            if (i < estado.cargas()) {
                desenhar(graphics, CARGA_CHEIA, x, y0, 1.0F);
                continue;
            }

            desenhar(graphics, CARGA_VAZIA, x, y0, 1.0F);
            if (i == estado.cargas() && progresso > 0.0F) {
                desenhar(graphics, CARGA_CHEIA, x, y0, progresso);
            }
        }
    }

    private static void desenhar(GuiGraphics graphics, ResourceLocation textura, int x, int y, float preenchimento) {
        int alturaFonte = Math.round(TAMANHO_TEXTURA * preenchimento);
        int alturaTela = Math.round(TAMANHO * preenchimento);
        if (alturaFonte <= 0 || alturaTela <= 0) {
            return;
        }
        graphics.blit(textura, x, y + TAMANHO - alturaTela, TAMANHO, alturaTela,
                0, TAMANHO_TEXTURA - alturaFonte, TAMANHO_TEXTURA, alturaFonte, TAMANHO_TEXTURA, TAMANHO_TEXTURA);
    }
}
