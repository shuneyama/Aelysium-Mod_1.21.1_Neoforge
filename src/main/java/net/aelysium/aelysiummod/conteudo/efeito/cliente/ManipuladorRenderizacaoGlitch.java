package net.aelysium.aelysiummod.conteudo.efeito.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class ManipuladorRenderizacaoGlitch {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            ManipuladorShaderCegueiraAbissal.processShader(partialTick);
            ManipuladorShaderCaecitas.processShader(partialTick);
        }
    }
}