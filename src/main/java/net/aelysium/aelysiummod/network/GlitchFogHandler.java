package net.aelysium.aelysiummod.network;

import com.mojang.blaze3d.shaders.FogShape;
import net.aelysium.aelysiummod.efeitos.ModEfeitos;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public class GlitchFogHandler {

    private static final float FOG_START = 64.0f;
    private static final float FOG_END = 160.0f;

    @SubscribeEvent
    public void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        boolean hasGlitch = player.hasEffect(ModEfeitos.CEGUEIRA_ABISSAL)
                || player.hasEffect(ModEfeitos.CAECITAS);
        if (!hasGlitch) return;

        event.setNearPlaneDistance(FOG_START);
        event.setFarPlaneDistance(FOG_END);
        event.setFogShape(FogShape.SPHERE);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        boolean hasGlitch = player.hasEffect(ModEfeitos.CEGUEIRA_ABISSAL)
                || player.hasEffect(ModEfeitos.CAECITAS);
        if (!hasGlitch) return;

        event.setRed(0.0f);
        event.setGreen(0.0f);
        event.setBlue(0.0f);
    }
}