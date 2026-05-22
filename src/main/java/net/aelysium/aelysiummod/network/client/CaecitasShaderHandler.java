package net.aelysium.aelysiummod.network.client;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.efeitos.ModEfeitos;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class CaecitasShaderHandler {

    private static final ResourceLocation SHADER_LOCATION =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "shaders/post/aelysium_caecitas.json");

    private static PostChain activeShader = null;
    private static boolean wasActive = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        boolean isActive = player != null && player.hasEffect(ModEfeitos.CAECITAS);

        if (isActive && !wasActive) {
            loadShader(mc);
        } else if (!isActive && wasActive) {
            removeShader();
        }

        if (isActive && activeShader != null) {
            activeShader.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());

            long time = mc.level != null ? mc.level.getGameTime() : 0;
        }

        wasActive = isActive;
    }

    private static void loadShader(Minecraft mc) {
        removeShader();
        try {
            mc.gameRenderer.loadEffect(SHADER_LOCATION);
            activeShader = mc.gameRenderer.currentEffect();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao carregar shader GlitchVermelho: ", e);
            activeShader = null;
        }
    }

    private static void removeShader() {
        if (activeShader != null) {
            Minecraft.getInstance().gameRenderer.shutdownEffect();
            activeShader = null;
        }
    }

    public static boolean processShader(float partialTick) {
        if (activeShader != null) {
            activeShader.process(partialTick);
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            return true;
        }
        return false;
    }

    public static boolean isActive() {
        return activeShader != null;
    }
}