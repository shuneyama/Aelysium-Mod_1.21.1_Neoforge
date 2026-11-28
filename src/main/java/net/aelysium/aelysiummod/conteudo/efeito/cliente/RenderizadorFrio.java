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
public final class RenderizadorFrio {

    private static final ResourceLocation NEVE = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/gui/frio_neve.png");
    private static final ResourceLocation VINHETA = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/gui/calor_vinheta.png");

    private static final int NEVE_TAMANHO = 128;
    private static final int QUADROS = 16;
    private static final int NEVE_ALTURA_TOTAL = NEVE_TAMANHO * QUADROS;
    private static final float TICKS_POR_QUADRO = 2.5F;
    private static final float ALTURA_POR_ESCALA = 180.0F;

    private RenderizadorFrio() {
    }

    @SubscribeEvent
    public static void aoRenderizar(RenderGuiEvent.Pre evento) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        MobEffectInstance efeito = mc.player.getEffect(ModEfeitos.FRIO);
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
            r = Mth.lerp(m, 0.45F, 0.62F);
            g = Mth.lerp(m, 0.78F, 0.88F);
            b = Mth.lerp(m, 0.95F, 1.0F);
        } else {
            float m = (k - 0.5F) * 2.0F;
            r = Mth.lerp(m, 0.62F, 0.82F);
            g = Mth.lerp(m, 0.88F, 0.96F);
            b = 1.0F;
        }
        float pulso = 0.85F + 0.15F * Mth.sin(tempo * 0.21F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(r, g, b, 0.7F * intensidade * pulso);
        tela.blit(VINHETA, 0, 0, largura, altura, 0.0F, 0.0F, 512, 512, 512, 512);

        int quadro = (int) (tempo / TICKS_POR_QUADRO) % QUADROS;
        int escala = Math.max(1, Math.round(altura / ALTURA_POR_ESCALA));
        int lado = NEVE_TAMANHO * escala;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.9F * intensidade);
        for (int x = 0; x < largura; x += lado) {
            for (int y = 0; y < altura; y += lado) {
                tela.blit(NEVE, x, y, lado, lado, 0.0F, (float) (quadro * NEVE_TAMANHO), NEVE_TAMANHO, NEVE_TAMANHO, NEVE_TAMANHO, NEVE_ALTURA_TOTAL);
            }
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
