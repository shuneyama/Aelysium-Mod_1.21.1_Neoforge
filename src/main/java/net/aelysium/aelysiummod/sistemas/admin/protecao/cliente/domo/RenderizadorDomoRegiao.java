package net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.domo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.RegiaoCacheCliente;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class RenderizadorDomoRegiao {

    private static final int LAT_SEGMENTS = 16;
    private static final int LON_SEGMENTS = 28;
    private static final float MARGEM_CULL = 15.0f;

    @SubscribeEvent
    public static void aoRenderizarMundo(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (!AelysiumGuarda.isAutorizadoAgora()) return;

        Minecraft mc = Minecraft.getInstance();
        Player jogador = mc.player;
        if (jogador == null || mc.level == null) return;

        ShaderInstance shader = ShadersDomo.domoRegiao();
        if (shader == null) return;

        List<RegiaoCacheCliente.RegiaoEntry> regioes =
                RegiaoCacheCliente.getEntriesPorDimensao(mc.level.dimension().location());
        if (regioes.isEmpty()) return;

        Vec3 camera = event.getCamera().getPosition();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        for (RegiaoCacheCliente.RegiaoEntry regiao : regioes) {
            if (!regiao.domoAtivo()) continue;
            renderizarDomo(mc, poseStack, shader, regiao, camera);
        }

        poseStack.popPose();
    }

    private static void renderizarDomo(Minecraft mc, PoseStack poseStack, ShaderInstance shader,
                                        RegiaoCacheCliente.RegiaoEntry regiao, Vec3 camera) {
        BlockPos min = regiao.min();
        BlockPos max = regiao.max();

        double distancia = distanciaAoAABB(camera, min, max);
        if (distancia > regiao.domoDistancia() + MARGEM_CULL) return;

        double centroX = (min.getX() + max.getX() + 1) / 2.0;
        double centroY = (min.getY() + max.getY() + 1) / 2.0;
        double centroZ = (min.getZ() + max.getZ() + 1) / 2.0;

        float rx = Math.abs(max.getX() - min.getX() + 1) / 2.0f;
        float ry = Math.abs(max.getY() - min.getY() + 1) / 2.0f;
        float rz = Math.abs(max.getZ() - min.getZ() + 1) / 2.0f;
        if (rx <= 0 || ry <= 0 || rz <= 0) return;

        float raio = (float) Math.sqrt(rx * rx + ry * ry + rz * rz) * 1.03f;

        int cor = regiao.domoCor();
        float corR = ((cor >> 16) & 0xFF) / 255.0f;
        float corG = ((cor >> 8) & 0xFF) / 255.0f;
        float corB = (cor & 0xFF) / 255.0f;

        setUniform(shader, "DistanciaFade", regiao.domoDistancia());
        setUniform(shader, "BaseColor", corR, corG, corB);

        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(TipoRenderizacaoDomo.domo());
        Matrix4f matriz = poseStack.last().pose();

        desenharElipsoide(matriz, buffer, (float) centroX, (float) centroY, (float) centroZ, raio, raio, raio);

        mc.renderBuffers().bufferSource().endBatch(TipoRenderizacaoDomo.domo());
    }

    private static double distanciaAoAABB(Vec3 camera, BlockPos min, BlockPos max) {
        double dx = Math.max(Math.max(min.getX() - camera.x, 0), camera.x - (max.getX() + 1));
        double dy = Math.max(Math.max(min.getY() - camera.y, 0), camera.y - (max.getY() + 1));
        double dz = Math.max(Math.max(min.getZ() - camera.z, 0), camera.z - (max.getZ() + 1));
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static void setUniform(ShaderInstance shader, String nome, float valor) {
        var uniforme = shader.getUniform(nome);
        if (uniforme != null) uniforme.set(valor);
    }

    private static void setUniform(ShaderInstance shader, String nome, float x, float y, float z) {
        var uniforme = shader.getUniform(nome);
        if (uniforme != null) uniforme.set(x, y, z);
    }

    private static void desenharElipsoide(Matrix4f matriz, VertexConsumer buffer,
                                          float cx, float cy, float cz,
                                          float rx, float ry, float rz) {
        for (int lat = 0; lat < LAT_SEGMENTS; lat++) {
            float theta1 = (float) (Math.PI * lat / LAT_SEGMENTS);
            float theta2 = (float) (Math.PI * (lat + 1) / LAT_SEGMENTS);

            for (int lon = 0; lon < LON_SEGMENTS; lon++) {
                float phi1 = (float) (2 * Math.PI * lon / LON_SEGMENTS);
                float phi2 = (float) (2 * Math.PI * (lon + 1) / LON_SEGMENTS);

                float[] n1 = direcaoEsfera(theta1, phi1);
                float[] n2 = direcaoEsfera(theta2, phi1);
                float[] n3 = direcaoEsfera(theta2, phi2);
                float[] n4 = direcaoEsfera(theta1, phi2);

                vertice(matriz, buffer, cx, cy, cz, rx, ry, rz, n1, 0f, 0f);
                vertice(matriz, buffer, cx, cy, cz, rx, ry, rz, n3, 1f, 1f);
                vertice(matriz, buffer, cx, cy, cz, rx, ry, rz, n2, 0f, 1f);

                vertice(matriz, buffer, cx, cy, cz, rx, ry, rz, n1, 0f, 0f);
                vertice(matriz, buffer, cx, cy, cz, rx, ry, rz, n4, 1f, 0f);
                vertice(matriz, buffer, cx, cy, cz, rx, ry, rz, n3, 1f, 1f);
            }
        }
    }

    private static float[] direcaoEsfera(float theta, float phi) {
        float sinTheta = (float) Math.sin(theta);
        return new float[]{
                sinTheta * (float) Math.cos(phi),
                (float) Math.cos(theta),
                sinTheta * (float) Math.sin(phi)
        };
    }

    private static void vertice(Matrix4f matriz, VertexConsumer buffer,
                                float cx, float cy, float cz, float rx, float ry, float rz,
                                float[] normal, float u, float v) {
        float x = cx + normal[0] * rx;
        float y = cy + normal[1] * ry;
        float z = cz + normal[2] * rz;

        float corR = normal[0] * 0.5f + 0.5f;
        float corG = normal[1] * 0.5f + 0.5f;
        float corB = normal[2] * 0.5f + 0.5f;

        buffer.addVertex(matriz, x, y, z).setUv(u, v).setColor(corR, corG, corB, 1.0f);
    }
}
