package net.aelysium.aelysiummod.sistemas.social.chat.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.social.chat.IJogadorAelysium;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class ManipuladorRenderizacaoBalao {

    public static boolean baloesVisiveis = true;

    private static long lastDebugLog = 0;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;

        if (!AelysiumGuarda.isAutorizadoAgora()) return;

        if (!baloesVisiveis) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Player localPlayer = mc.player;

        if (localPlayer.isSpectator()) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        long now = System.currentTimeMillis();
        boolean shouldLog = (now - lastDebugLog) > 3000;

        for (AbstractClientPlayer player : mc.level.players()) {
            if (player.isInvisible() || !player.isAlive() || player.isSpectator()) continue;

            if (player != localPlayer && !temLinhaDeVisao(localPlayer, player, partialTick)) continue;

            IJogadorAelysium playerMixin = (IJogadorAelysium) player;
            List<IJogadorAelysium.MensagemBalao> balloons = playerMixin.aelysium$getBalloons();

            if (balloons == null || balloons.isEmpty()) continue;

            event.getPoseStack().pushPose();

            Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();

            double x = Mth.lerp(partialTick, player.xo, player.getX());
            double y = Mth.lerp(partialTick, player.yo, player.getY());
            double z = Mth.lerp(partialTick, player.zo, player.getZ());

            event.getPoseStack().translate(
                    x - camera.x,
                    y - camera.y,
                    z - camera.z
            );

            RenderizadorBalao.renderBalloons(
                    event.getPoseStack(),
                    mc.getEntityRenderDispatcher(),
                    mc.font,
                    balloons,
                    player.getBbHeight()
            );

            event.getPoseStack().popPose();
        }

        if (shouldLog) lastDebugLog = now;
    }

    private static boolean temLinhaDeVisao(Player observador, Player alvo, float partialTick) {
        Vec3 olhos = observador.getEyePosition(partialTick);
        Vec3 alvoPos = alvo.getEyePosition(partialTick);

        ClipContext clipContext = new ClipContext(
                olhos,
                alvoPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                observador
        );

        return observador.level().clip(clipContext).getType() == HitResult.Type.MISS;
    }
}