package net.aelysium.aelysiummod.conteudo.item.escudo.cliente;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.entidade.EntidadeBarreiraPainel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class RenderizadorPainelBarreira extends EntityRenderer<EntidadeBarreiraPainel> {

    private static final ResourceLocation TEXTURA = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/entity/barreira_abismo.png");
    private static final int LUZ_MAXIMA = 15728880;
    private static final float METADE = 2.5F;

    private static final int[] COR_CLARA = {0xDC, 0x14, 0x3C};
    private static final int[] COR_ESCURA = {0xAD, 0x00, 0x34};

    public RenderizadorPainelBarreira(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntidadeBarreiraPainel entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        int face = entity.getFace();
        if (face < 2) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        } else if (face < 4) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        }

        float tempo = entity.tickCount + partialTick;
        float base = entity.getId() * 0.73F + face * 0.41F;
        float[] fatores = {
                ruido(tempo, base),
                ruido(tempo, base + 1.9F),
                ruido(tempo, base + 3.8F),
                ruido(tempo, base + 5.7F)
        };

        VertexConsumer consumidor = buffer.getBuffer(RenderType.entityTranslucent(TEXTURA));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matriz = pose.pose();

        vertice(consumidor, matriz, pose, -METADE, -METADE, 0.0F, 0.0F, 1.0F, fatores[0]);
        vertice(consumidor, matriz, pose, METADE, -METADE, 0.0F, 1.0F, 1.0F, fatores[1]);
        vertice(consumidor, matriz, pose, METADE, METADE, 0.0F, 1.0F, 0.0F, fatores[2]);
        vertice(consumidor, matriz, pose, -METADE, METADE, 0.0F, 0.0F, 0.0F, fatores[3]);

        vertice(consumidor, matriz, pose, -METADE, METADE, 0.0F, 0.0F, 0.0F, fatores[3]);
        vertice(consumidor, matriz, pose, METADE, METADE, 0.0F, 1.0F, 0.0F, fatores[2]);
        vertice(consumidor, matriz, pose, METADE, -METADE, 0.0F, 1.0F, 1.0F, fatores[1]);
        vertice(consumidor, matriz, pose, -METADE, -METADE, 0.0F, 0.0F, 1.0F, fatores[0]);

        poseStack.popPose();
    }

    private static float ruido(float tempo, float fase) {
        float valor = 0.5F
                + 0.25F * Mth.sin(tempo * 0.21F + fase)
                + 0.15F * Mth.sin(tempo * 0.53F + fase * 1.7F)
                + 0.10F * Mth.sin(tempo * 1.31F + fase * 2.3F);
        return Mth.clamp(valor, 0.0F, 1.0F);
    }

    private static void vertice(VertexConsumer consumidor, Matrix4f matriz, PoseStack.Pose pose, float x, float y, float z, float u, float v, float fator) {
        int r = Mth.floor(Mth.lerp(fator, COR_CLARA[0], COR_ESCURA[0]));
        int g = Mth.floor(Mth.lerp(fator, COR_CLARA[1], COR_ESCURA[1]));
        int b = Mth.floor(Mth.lerp(fator, COR_CLARA[2], COR_ESCURA[2]));
        consumidor.addVertex(matriz, x, y, z)
                .setColor(r, g, b, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LUZ_MAXIMA)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntidadeBarreiraPainel entity) {
        return TEXTURA;
    }
}
