package net.aelysium.aelysiummod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@OnlyIn(Dist.CLIENT)
public class ValkyriaHudRenderer {

    private static final int MAX_VOO_TICKS = 20 * 60 * 3;
    private static final int ICON_SIZE     = 16;

    private static final ResourceLocation WINGS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("aelysiummod", "textures/gui/asas.png");

    private static boolean visible      = false;
    private static int     ticksFlown   = 0;
    private static int     cooldownTicks = 0;

    public static void update(boolean vis, int flown, int cd) {
        visible       = vis;
        ticksFlown    = flown;
        cooldownTicks = cd;
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        if (!visible) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        GuiGraphics gfx = event.getGuiGraphics();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        boolean onCooldown = cooldownTicks > 0;
        String timeText    = buildTimeText(onCooldown);
        int color          = onCooldown ? 0xFFFF4444 : 0xFFFFFFFF;

        int textW      = mc.font.width(timeText);
        int blockW     = Math.max(ICON_SIZE, textW);

        int healthBarLeft = screenW / 2 - 91;
        int rightEdge     = healthBarLeft - 4;
        int leftEdge      = rightEdge - blockW;

        int baseY  = screenH - 33;
        int iconY  = baseY - ICON_SIZE - 2;
        int textY  = baseY - mc.font.lineHeight + 3;

        int iconX  = leftEdge + (blockW - ICON_SIZE) / 2;
        int textX  = leftEdge + (blockW - textW) / 2;

        RenderSystem.setShaderColor(
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8)  & 0xFF) / 255f,
                ( color        & 0xFF) / 255f,
                1.0f
        );
        gfx.blit(WINGS_TEXTURE, iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        gfx.drawString(mc.font, timeText, textX, textY, color, true);
    }

    private static String buildTimeText(boolean onCooldown) {
        int secsLeft;
        if (onCooldown) {
            secsLeft = (cooldownTicks + 19) / 20;
        } else {
            int ticksLeft = MAX_VOO_TICKS - ticksFlown;
            secsLeft = (ticksLeft + 19) / 20;
        }

        if (secsLeft >= 60) {
            int min = secsLeft / 60;
            int sec = secsLeft % 60;
            return String.format("%02d:%02d", min, sec);
        }

        return String.format("%02d", secsLeft);
    }
}