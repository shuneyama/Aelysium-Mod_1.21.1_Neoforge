package net.aelysium.aelysiummod.conteudo.efeito.cliente;

import com.mojang.blaze3d.systems.RenderSystem;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModEfeitos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public final class RenderizadorCalor {

    private static final ResourceLocation FOGO = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/gui/calor_fogo.png");
    private static final ResourceLocation VINHETA = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/gui/calor_vinheta.png");

    private static final int FOGO_LARGURA = 246;
    private static final int FOGO_ALTURA_QUADRO = 54;
    private static final int FOGO_ALTURA_UTIL = 52;
    private static final int FOGO_ALTURA_TOTAL = 324;
    private static final int QUADROS = 6;
    private static final float TICKS_POR_QUADRO = 1.5F;
    private static final float FRACAO_ALTURA_FOGO = 0.22F;

    private RenderizadorCalor() {
    }

    @SubscribeEvent
    public static void aoRenderizar(RenderGuiEvent.Pre evento) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        MobEffectInstance efeito = mc.player.getEffect(ModEfeitos.CALOR);
        if (efeito == null) return;

        float tempo = mc.level.getGameTime() + evento.getPartialTick().getGameTimeDeltaPartialTick(false);
        float intensidade = efeito.isInfiniteDuration() ? 1.0F : Mth.clamp(efeito.getDuration() / 30.0F, 0.0F, 1.0F);

        GuiGraphics tela = evento.getGuiGraphics();
        int largura = mc.getWindow().getGuiScaledWidth();
        int altura = mc.getWindow().getGuiScaledHeight();

        float k = 0.5F + 0.3F * Mth.sin(tempo * 0.07F) + 0.12F * Mth.sin(tempo * 0.173F + 1.3F) + 0.08F * Mth.sin(tempo * 0.31F + 2.1F);
        k = Mth.clamp(k, 0.0F, 1.0F);
        float r;
        float g;
        float b;
        if (k < 0.5F) {
            float m = k * 2.0F;
            r = Mth.lerp(m, 0.82F, 1.0F);
            g = Mth.lerp(m, 0.33F, 0.49F);
            b = Mth.lerp(m, 0.04F, 0.08F);
        } else {
            float m = (k - 0.5F) * 2.0F;
            r = 1.0F;
            g = Mth.lerp(m, 0.49F, 0.65F);
            b = Mth.lerp(m, 0.08F, 0.27F);
        }
        float pulso = 0.85F + 0.15F * Mth.sin(tempo * 0.21F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(r, g, b, 0.7F * intensidade * pulso);
        tela.blit(VINHETA, 0, 0, largura, altura, 0.0F, 0.0F, 512, 512, 512, 512);

        float posicao = tempo / TICKS_POR_QUADRO;
        int quadro = (int) posicao % QUADROS;
        int proximo = (quadro + 1) % QUADROS;
        float mistura = posicao - (float) Math.floor(posicao);
        mistura = mistura * mistura * (3.0F - 2.0F * mistura);
        int escala = Math.max(1, Math.round(altura * FRACAO_ALTURA_FOGO / FOGO_ALTURA_UTIL));
        int larguraFaixa = FOGO_LARGURA * escala;
        int alturaFaixa = FOGO_ALTURA_UTIL * escala;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, intensidade * (1.0F - mistura * mistura));
        desenharFaixa(tela, quadro, largura, altura, larguraFaixa, alturaFaixa);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, intensidade * mistura);
        desenharFaixa(tela, proximo, largura, altura, larguraFaixa, alturaFaixa);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static void desenharFaixa(GuiGraphics tela, int quadro, int largura, int altura, int larguraFaixa, int alturaFaixa) {
        for (int x = 0; x < largura; x += larguraFaixa) {
            tela.blit(FOGO, x, altura - alturaFaixa, larguraFaixa, alturaFaixa, 0.0F, (float) (quadro * FOGO_ALTURA_QUADRO), FOGO_LARGURA, FOGO_ALTURA_UTIL, FOGO_LARGURA, FOGO_ALTURA_TOTAL);
        }
    }
}
