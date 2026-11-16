package net.aelysium.aelysiummod.sistemas.mundo.minifarol.cliente;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.aelysium.aelysiummod.sistemas.mundo.minifarol.EntidadeMiniFarol;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class RenderizadorMiniFarol implements BlockEntityRenderer<EntidadeMiniFarol> {

    private static final float RAIO_FEIXE = 0.2F;
    private static final float RAIO_BRILHO = 0.25F;
    private static final int ALTURA = 1024;
    private static final int ALFA_FEIXE = 255;
    private static final int ALFA_BRILHO = 32;

    @Override
    public void render(EntidadeMiniFarol be, float parcial, PoseStack pose, MultiBufferSource buffers, int luz, int overlay) {
        if (!be.isAtivo() || be.getLevel() == null) return;
        long tempo = be.getLevel().getGameTime();
        int cor = be.getCor() & 0xFFFFFF;

        pose.pushPose();
        pose.translate(0.5, 0.0, 0.5);
        float t = (float) Math.floorMod(tempo, 40L) + parcial;
        float f2 = Mth.frac(-t * 0.2F - (float) Mth.floor(-t * 0.1F));
        float vMin = -1.0F + f2;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(t * 2.25F - 45.0F));
        parte(pose, buffers.getBuffer(RenderType.beaconBeam(BeaconRenderer.BEAM_LOCATION, false)),
                FastColor.ARGB32.color(ALFA_FEIXE, cor), vMin, ALTURA * (0.5F / RAIO_FEIXE) + vMin,
                0.0F, RAIO_FEIXE, RAIO_FEIXE, 0.0F, -RAIO_FEIXE, 0.0F, 0.0F, -RAIO_FEIXE);
        pose.popPose();

        parte(pose, buffers.getBuffer(RenderType.beaconBeam(BeaconRenderer.BEAM_LOCATION, true)),
                FastColor.ARGB32.color(ALFA_BRILHO, cor), vMin, ALTURA + vMin,
                -RAIO_BRILHO, -RAIO_BRILHO, RAIO_BRILHO, -RAIO_BRILHO, -RAIO_BRILHO, RAIO_BRILHO, RAIO_BRILHO, RAIO_BRILHO);
        pose.popPose();
    }

    private static void parte(PoseStack pose, VertexConsumer c, int cor, float vMin, float vMax,
                              float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4) {
        PoseStack.Pose p = pose.last();
        quad(p, c, cor, x1, z1, x2, z2, vMin, vMax);
        quad(p, c, cor, x4, z4, x3, z3, vMin, vMax);
        quad(p, c, cor, x2, z2, x4, z4, vMin, vMax);
        quad(p, c, cor, x3, z3, x1, z1, vMin, vMax);
    }

    private static void quad(PoseStack.Pose p, VertexConsumer c, int cor,
                             float minX, float minZ, float maxX, float maxZ, float vBaixo, float vCima) {
        vertice(p, c, cor, ALTURA, minX, minZ, 1.0F, vCima);
        vertice(p, c, cor, 0.0F, minX, minZ, 1.0F, vBaixo);
        vertice(p, c, cor, 0.0F, maxX, maxZ, 0.0F, vBaixo);
        vertice(p, c, cor, ALTURA, maxX, maxZ, 0.0F, vCima);
    }

    private static void vertice(PoseStack.Pose p, VertexConsumer c, int cor, float y, float x, float z, float u, float v) {
        c.addVertex(p, x, y, z).setColor(cor).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(p, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public AABB getRenderBoundingBox(EntidadeMiniFarol be) {
        BlockPos p = be.getBlockPos();
        return new AABB(p.getX() - 1, p.getY(), p.getZ() - 1, p.getX() + 2, p.getY() + ALTURA, p.getZ() + 2);
    }

    @Override
    public boolean shouldRenderOffScreen(EntidadeMiniFarol be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
