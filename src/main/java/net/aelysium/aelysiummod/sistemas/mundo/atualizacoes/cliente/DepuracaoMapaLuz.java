package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente;

import com.mojang.blaze3d.platform.InputConstants;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class DepuracaoMapaLuz {

    private static boolean ativo;

    @SubscribeEvent
    public static void tecla(InputEvent.Key evento) {
        if (evento.getAction() != GLFW.GLFW_PRESS || evento.getKey() != GLFW.GLFW_KEY_4) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_F3)) return;
        ativo = !ativo;
        minecraft.gui.getChat().addMessage(Component.translatable(ativo ? "aelysiummod.depuracao.mapa_luz.ligado" : "aelysiummod.depuracao.mapa_luz.desligado"));
    }

    @SubscribeEvent
    public static void desenhar(RenderGuiEvent.Post evento) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ativo || minecraft.level == null) return;
        ResourceLocation textura = minecraft.gameRenderer.lightTexture().lightTextureLocation;
        GuiGraphics grafico = evento.getGuiGraphics();
        int tamanho = 64;
        int x = grafico.guiWidth() - tamanho - 4;
        int y = grafico.guiHeight() - tamanho - 4;
        grafico.fill(x - 1, y - 1, x + tamanho + 1, y + tamanho + 1, 0xFF000000);
        grafico.blit(textura, x, y, tamanho, tamanho, 0.0F, 16.0F, 16, -16, 16, 16);
    }
}
