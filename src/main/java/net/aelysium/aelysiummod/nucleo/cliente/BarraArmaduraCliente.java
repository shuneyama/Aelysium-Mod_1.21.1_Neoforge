package net.aelysium.aelysiummod.nucleo.cliente;

import com.mojang.blaze3d.systems.RenderSystem;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public final class BarraArmaduraCliente {
    private static final ResourceLocation CAMADA = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "armadura_extra");
    private static final ResourceLocation CHEIO = ResourceLocation.withDefaultNamespace("hud/armor_full");
    private static final ResourceLocation METADE = ResourceLocation.withDefaultNamespace("hud/armor_half");
    private static final int PONTOS_VANILLA = 20;

    private BarraArmaduraCliente() {
    }

    @SubscribeEvent
    public static void registrar(RegisterGuiLayersEvent evento) {
        evento.registerAbove(VanillaGuiLayers.ARMOR_LEVEL, CAMADA, BarraArmaduraCliente::desenhar);
    }

    private static void desenhar(GuiGraphics g, DeltaTracker tempo) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || !mc.gameMode.canHurtPlayer()) return;
        if (!(mc.getCameraEntity() instanceof Player jogador)) return;
        int extra = jogador.getArmorValue() - PONTOS_VANILLA;
        if (extra <= 0) return;
        int x0 = g.guiWidth() / 2 - 91;
        int y = g.guiHeight() - mc.gui.leftHeight + 10;
        RenderSystem.enableBlend();
        g.setColor(0.3F, 0.85F, 1.0F, 1.0F);
        for (int i = 0; i < 10; i++) {
            int x = x0 + i * 8;
            if (i * 2 + 1 < extra) {
                g.blitSprite(CHEIO, x, y, 9, 9);
            } else if (i * 2 + 1 == extra) {
                g.blitSprite(METADE, x, y, 9, 9);
            }
        }
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
