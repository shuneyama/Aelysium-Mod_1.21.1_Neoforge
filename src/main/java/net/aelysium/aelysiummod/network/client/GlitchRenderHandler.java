package net.aelysium.aelysiummod.network.client;

import net.aelysium.aelysiummod.AelysiumMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class GlitchRenderHandler {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            CegueiraAbissalShaderHandler.processShader(partialTick);
            CaecitasShaderHandler.processShader(partialTick);
        }
    }
}