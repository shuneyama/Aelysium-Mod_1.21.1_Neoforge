package net.aelysium.aelysiummod.npc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.aelysium.aelysiummod.npc.client.skin.NpcSkinManager;
import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class CustomNpcRenderer extends EntityRenderer<CustomNpcEntity> {

    private final PlayerRenderer wideRenderer;
    private final PlayerRenderer slimRenderer;

    public CustomNpcRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.wideRenderer = new PlayerRenderer(ctx, false);
        this.slimRenderer = new PlayerRenderer(ctx, true);
    }

    @Override
    public void render(CustomNpcEntity npc, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        FakeNpcPlayer fakePlayer = FakeNpcPlayer.getOrCreate(npc);
        if (fakePlayer == null) return;

        ResourceLocation customSkin = NpcSkinManager.getSkinTexture(npc.getId());
        fakePlayer.setCustomSkinTexture(customSkin);

        boolean isSlim = "slim".equals(npc.getModelType());
        PlayerRenderer renderer = isSlim ? slimRenderer : wideRenderer;
        enableOverlayLayers(renderer);

        float sx = npc.getScaleX();
        float sy = npc.getScaleY();
        float sz = npc.getScaleZ();
        float rx = npc.getRotationX();
        float ry = npc.getRotationY();
        float rz = npc.getRotationZ();

        boolean hasTransform = sx != 1.0f || sy != 1.0f || sz != 1.0f
                || rx != 0f || ry != 0f || rz != 0f;

        if (hasTransform) {
            poseStack.pushPose();
            if (rx != 0f) poseStack.mulPose(Axis.XP.rotationDegrees(rx));
            if (ry != 0f) poseStack.mulPose(Axis.YP.rotationDegrees(ry));
            if (rz != 0f) poseStack.mulPose(Axis.ZP.rotationDegrees(rz));
            poseStack.scale(sx, sy, sz);
        }

        renderer.render(fakePlayer, entityYaw, partialTick, poseStack, buffer, packedLight);

        if (hasTransform) {
            poseStack.popPose();
        }

        double distSq = this.entityRenderDispatcher.distanceToSqr(npc);
        if (npc.getNameVisible() && distSq < 32) {
            renderNpcNameTag(npc, poseStack, buffer, packedLight);
        }
    }


    private void renderNpcNameTag(CustomNpcEntity npc, PoseStack poseStack,
                                  MultiBufferSource buffer, int packedLight) {
        Component displayName = npc.getDisplayName();
        String rawName = npc.getNpcName();
        if (rawName == null || rawName.isBlank()) return;

        float sy = npc.getScaleY();
        float nameHeight = (npc.getBbHeight() * sy) + 0.5f;

        poseStack.pushPose();
        poseStack.translate(0.0, nameHeight, 0.0);

        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.025f, -0.025f, 0.025f);

        Font font = Minecraft.getInstance().font;
        float textWidth = font.width(displayName);
        float x = -textWidth / 2.0f;

        Matrix4f matrix = poseStack.last().pose();

        int bgAlpha = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25f) * 255.0f);
        int bgColor = bgAlpha << 24;

        font.drawInBatch(displayName, x, 0, 0x20FFFFFF, false,
                matrix, buffer, Font.DisplayMode.SEE_THROUGH, bgColor, packedLight);

        font.drawInBatch(displayName, x, 0, -1, false,
                matrix, buffer, Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();
    }

    private void enableOverlayLayers(PlayerRenderer renderer) {
        try {
            PlayerModel<?> model = renderer.getModel();
            if (model.hat != null) model.hat.visible = true;
            if (model.jacket != null) model.jacket.visible = true;
            if (model.leftSleeve != null) model.leftSleeve.visible = true;
            if (model.rightSleeve != null) model.rightSleeve.visible = true;
            if (model.leftPants != null) model.leftPants.visible = true;
            if (model.rightPants != null) model.rightPants.visible = true;
        } catch (Exception ignored) {}
    }

    @Override
    protected void renderNameTag(CustomNpcEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {

    }

    @Override
    public ResourceLocation getTextureLocation(CustomNpcEntity entity) {
        ResourceLocation customSkin = NpcSkinManager.getSkinTexture(entity.getId());
        if (customSkin != null) return customSkin;
        return "slim".equals(entity.getModelType())
                ? ResourceLocation.withDefaultNamespace("textures/entity/player/slim/alex.png")
                : ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
    }
}
