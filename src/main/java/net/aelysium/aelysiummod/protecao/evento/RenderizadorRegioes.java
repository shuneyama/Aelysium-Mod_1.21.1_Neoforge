package net.aelysium.aelysiummod.protecao.evento;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.item.custom.VarinhaProtecao;
import net.aelysium.aelysiummod.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.protecao.regiao.Regiao;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.Collection;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class RenderizadorRegioes {

    private static final float[][] CORES = {
            {1.0f, 0.2f, 0.2f, 0.8f},
            {0.2f, 1.0f, 0.2f, 0.8f},
            {0.2f, 0.4f, 1.0f, 0.8f},
            {1.0f, 1.0f, 0.2f, 0.8f},
            {1.0f, 0.5f, 0.0f, 0.8f},
            {0.8f, 0.2f, 1.0f, 0.8f},
            {0.0f, 1.0f, 1.0f, 0.8f},
            {1.0f, 0.4f, 0.7f, 0.8f},
    };

    @SubscribeEvent
    public static void aoRenderizarMundo(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        Player jogador = mc.player;
        if (jogador == null) return;

        boolean temOP = jogador.hasPermissions(2);
        boolean noCriativo = mc.gameMode != null && mc.gameMode.getPlayerMode() == GameType.CREATIVE;
        boolean segurandoVarinha = jogador.getMainHandItem().getItem() instanceof VarinhaProtecao
                || jogador.getOffhandItem().getItem() instanceof VarinhaProtecao;

        if (!temOP || !noCriativo || !segurandoVarinha) return;

        Collection<Regiao> regioes = GerenciadorRegioes.getInstance().getTodasRegioes();
        if (regioes.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        var camera = event.getCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
        Matrix4f matriz = poseStack.last().pose();
        var pose = poseStack.last();

        int indice = 0;
        for (Regiao regiao : regioes) {
            float[] cor = CORES[indice % CORES.length];
            float r = cor[0], g = cor[1], b = cor[2], a = cor[3];
            indice++;

            BlockPos min = regiao.getPosicaoMinima();
            BlockPos max = regiao.getPosicaoMaxima();

            float x1 = min.getX();
            float y1 = min.getY();
            float z1 = min.getZ();
            float x2 = max.getX() + 1.0f;
            float y2 = max.getY() + 1.0f;
            float z2 = max.getZ() + 1.0f;

            linha(buffer, matriz, pose, x1, y1, z1, x2, y1, z1, r, g, b, a);
            linha(buffer, matriz, pose, x2, y1, z1, x2, y1, z2, r, g, b, a);
            linha(buffer, matriz, pose, x2, y1, z2, x1, y1, z2, r, g, b, a);
            linha(buffer, matriz, pose, x1, y1, z2, x1, y1, z1, r, g, b, a);

            linha(buffer, matriz, pose, x1, y2, z1, x2, y2, z1, r, g, b, a);
            linha(buffer, matriz, pose, x2, y2, z1, x2, y2, z2, r, g, b, a);
            linha(buffer, matriz, pose, x2, y2, z2, x1, y2, z2, r, g, b, a);
            linha(buffer, matriz, pose, x1, y2, z2, x1, y2, z1, r, g, b, a);

            linha(buffer, matriz, pose, x1, y1, z1, x1, y2, z1, r, g, b, a);
            linha(buffer, matriz, pose, x2, y1, z1, x2, y2, z1, r, g, b, a);
            linha(buffer, matriz, pose, x2, y1, z2, x2, y2, z2, r, g, b, a);
            linha(buffer, matriz, pose, x1, y1, z2, x1, y2, z2, r, g, b, a);
        }

        mc.renderBuffers().bufferSource().endBatch(RenderType.lines());
        poseStack.popPose();
    }

    private static void linha(VertexConsumer buffer, Matrix4f matriz, PoseStack.Pose pose,
                              float x1, float y1, float z1, float x2, float y2, float z2,
                              float r, float g, float b, float a) {
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len == 0) return;
        dx /= len; dy /= len; dz /= len;

        buffer.addVertex(matriz, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, dx, dy, dz);
        buffer.addVertex(matriz, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, dx, dy, dz);
    }
}
