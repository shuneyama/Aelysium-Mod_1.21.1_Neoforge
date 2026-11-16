package net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.domo;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.RegiaoCacheCliente;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class RenderizadorDomoRegiao {

    private static final int LAT_SEGMENTS = 16;
    private static final int LON_SEGMENTS = 28;
    private static final float MARGEM_CULL = 15.0f;

    private static VertexBuffer malha;

    private static ShaderInstance shaderCache;
    private static Uniform uniformDistanciaFade;
    private static Uniform uniformBaseColor;
    private static Uniform uniformRaio;
    private static Uniform uniformDeslocamento;

    private static float ultimaDistanciaFade;
    private static float ultimoCorR;
    private static float ultimoCorG;
    private static float ultimoCorB;
    private static float ultimoRaio;
    private static float ultimoDeslocX;
    private static float ultimoDeslocY;
    private static float ultimoDeslocZ;

    @SubscribeEvent
    public static void aoRenderizarMundo(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (!AelysiumGuarda.isAutorizadoAgora()) return;

        Minecraft mc = Minecraft.getInstance();
        Player jogador = mc.player;
        if (jogador == null || mc.level == null) return;

        ShaderInstance shader = ShadersDomo.domoRegiao();
        if (shader == null) return;

        List<RegiaoCacheCliente.RegiaoEntry> domos =
                RegiaoCacheCliente.getDomosAtivos(mc.level.dimension().location());
        if (domos.isEmpty()) return;

        Vec3 camera = event.getCamera().getPosition();
        Frustum frustum = event.getFrustum();

        RenderType tipo = TipoRenderizacaoDomo.domo();
        boolean preparado = false;

        for (int i = 0, n = domos.size(); i < n; i++) {
            RegiaoCacheCliente.RegiaoEntry regiao = domos.get(i);

            BlockPos min = regiao.min();
            BlockPos max = regiao.max();

            double distancia = distanciaAoAABB(camera, min, max);
            if (distancia > regiao.domoDistancia() + MARGEM_CULL) continue;

            double centroX = (min.getX() + max.getX() + 1) / 2.0;
            double centroY = (min.getY() + max.getY() + 1) / 2.0;
            double centroZ = (min.getZ() + max.getZ() + 1) / 2.0;

            float rx = Math.abs(max.getX() - min.getX() + 1) / 2.0f;
            float ry = Math.abs(max.getY() - min.getY() + 1) / 2.0f;
            float rz = Math.abs(max.getZ() - min.getZ() + 1) / 2.0f;
            if (rx <= 0 || ry <= 0 || rz <= 0) continue;

            float raio = (float) Math.sqrt(rx * rx + ry * ry + rz * rz) * 1.03f;

            if (frustum != null && !frustum.isVisible(new AABB(
                    centroX - raio, centroY - raio, centroZ - raio,
                    centroX + raio, centroY + raio, centroZ + raio))) continue;

            if (!preparado) {
                atualizarUniformes(shader);
                if (malha == null || malha.isInvalid()) malha = construirMalha();
                tipo.setupRenderState();
                malha.bind();
                preparado = true;
            }

            int cor = regiao.domoCor();
            enviarDistanciaFade(regiao.domoDistancia());
            enviarCor(((cor >> 16) & 0xFF) / 255.0f, ((cor >> 8) & 0xFF) / 255.0f, (cor & 0xFF) / 255.0f);
            enviarRaio(raio);
            enviarDeslocamento((float) (centroX - camera.x), (float) (centroY - camera.y), (float) (centroZ - camera.z));

            malha.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), shader);
        }

        if (preparado) {
            VertexBuffer.unbind();
            tipo.clearRenderState();
        }
    }

    @SubscribeEvent
    public static void aoSairDoMundo(ClientPlayerNetworkEvent.LoggingOut event) {
        if (malha != null) {
            malha.close();
            malha = null;
        }
        shaderCache = null;
    }

    private static double distanciaAoAABB(Vec3 camera, BlockPos min, BlockPos max) {
        double dx = Math.max(Math.max(min.getX() - camera.x, 0), camera.x - (max.getX() + 1));
        double dy = Math.max(Math.max(min.getY() - camera.y, 0), camera.y - (max.getY() + 1));
        double dz = Math.max(Math.max(min.getZ() - camera.z, 0), camera.z - (max.getZ() + 1));
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static void atualizarUniformes(ShaderInstance shader) {
        if (shader == shaderCache) return;
        shaderCache = shader;
        uniformDistanciaFade = shader.getUniform("DistanciaFade");
        uniformBaseColor = shader.getUniform("BaseColor");
        uniformRaio = shader.getUniform("Raio");
        uniformDeslocamento = shader.getUniform("Deslocamento");
        ultimaDistanciaFade = Float.NaN;
        ultimoCorR = Float.NaN;
        ultimoCorG = Float.NaN;
        ultimoCorB = Float.NaN;
        ultimoRaio = Float.NaN;
        ultimoDeslocX = Float.NaN;
        ultimoDeslocY = Float.NaN;
        ultimoDeslocZ = Float.NaN;
    }

    private static void enviarDistanciaFade(float valor) {
        if (uniformDistanciaFade == null || valor == ultimaDistanciaFade) return;
        uniformDistanciaFade.set(valor);
        ultimaDistanciaFade = valor;
    }

    private static void enviarCor(float r, float g, float b) {
        if (uniformBaseColor == null || (r == ultimoCorR && g == ultimoCorG && b == ultimoCorB)) return;
        uniformBaseColor.set(r, g, b);
        ultimoCorR = r;
        ultimoCorG = g;
        ultimoCorB = b;
    }

    private static void enviarRaio(float valor) {
        if (uniformRaio == null || valor == ultimoRaio) return;
        uniformRaio.set(valor);
        ultimoRaio = valor;
    }

    private static void enviarDeslocamento(float x, float y, float z) {
        if (uniformDeslocamento == null || (x == ultimoDeslocX && y == ultimoDeslocY && z == ultimoDeslocZ)) return;
        uniformDeslocamento.set(x, y, z);
        ultimoDeslocX = x;
        ultimoDeslocY = y;
        ultimoDeslocZ = z;
    }

    private static VertexBuffer construirMalha() {
        int vertices = LAT_SEGMENTS * LON_SEGMENTS * 6;
        try (ByteBufferBuilder memoria = new ByteBufferBuilder(vertices * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(memoria, VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);

            for (int lat = 0; lat < LAT_SEGMENTS; lat++) {
                float theta1 = (float) (Math.PI * lat / LAT_SEGMENTS);
                float theta2 = (float) (Math.PI * (lat + 1) / LAT_SEGMENTS);
                float sin1 = (float) Math.sin(theta1);
                float sin2 = (float) Math.sin(theta2);
                float cos1 = (float) Math.cos(theta1);
                float cos2 = (float) Math.cos(theta2);

                for (int lon = 0; lon < LON_SEGMENTS; lon++) {
                    float phi1 = (float) (2 * Math.PI * lon / LON_SEGMENTS);
                    float phi2 = (float) (2 * Math.PI * (lon + 1) / LON_SEGMENTS);
                    float cosPhi1 = (float) Math.cos(phi1);
                    float sinPhi1 = (float) Math.sin(phi1);
                    float cosPhi2 = (float) Math.cos(phi2);
                    float sinPhi2 = (float) Math.sin(phi2);

                    float n1x = sin1 * cosPhi1, n1y = cos1, n1z = sin1 * sinPhi1;
                    float n2x = sin2 * cosPhi1, n2y = cos2, n2z = sin2 * sinPhi1;
                    float n3x = sin2 * cosPhi2, n3y = cos2, n3z = sin2 * sinPhi2;
                    float n4x = sin1 * cosPhi2, n4y = cos1, n4z = sin1 * sinPhi2;

                    vertice(builder, n1x, n1y, n1z, 0f, 0f);
                    vertice(builder, n3x, n3y, n3z, 1f, 1f);
                    vertice(builder, n2x, n2y, n2z, 0f, 1f);

                    vertice(builder, n1x, n1y, n1z, 0f, 0f);
                    vertice(builder, n4x, n4y, n4z, 1f, 0f);
                    vertice(builder, n3x, n3y, n3z, 1f, 1f);
                }
            }

            VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            buffer.bind();
            buffer.upload(builder.buildOrThrow());
            VertexBuffer.unbind();
            return buffer;
        }
    }

    private static void vertice(BufferBuilder builder, float nx, float ny, float nz, float u, float v) {
        builder.addVertex(nx, ny, nz).setUv(u, v).setColor(nx * 0.5f + 0.5f, ny * 0.5f + 0.5f, nz * 0.5f + 0.5f, 1.0f);
    }
}
