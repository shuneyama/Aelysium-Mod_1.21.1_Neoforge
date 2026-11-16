package net.aelysium.aelysiummod.sistemas.social.npc.cliente;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.aelysium.aelysiummod.nucleo.cliente.VersaoFonteCliente;
import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.WeakHashMap;

@OnlyIn(Dist.CLIENT)
public class RenderizadorNpcPersonalizado extends EntityRenderer<EntidadeNpcPersonalizado> {

    private final PlayerRenderer wideRenderer;
    private final PlayerRenderer slimRenderer;
    private final RenderizadorCorpoNpc corpoLargo;
    private final RenderizadorCorpoNpc corpoFino;

    private final Map<EntidadeNpcPersonalizado, CacheNome> cacheNomes = new WeakHashMap<>();

    private static final class CacheNome {
        Component componente;
        int versaoFonte;
        float metadeLargura;
    }

    public RenderizadorNpcPersonalizado(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.wideRenderer = new PlayerRenderer(ctx, false);
        this.slimRenderer = new PlayerRenderer(ctx, true);
        this.corpoLargo = new RenderizadorCorpoNpc(ctx, false);
        this.corpoFino = new RenderizadorCorpoNpc(ctx, true);
    }

    @Override
    public void render(EntidadeNpcPersonalizado npc, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        JogadorNpcFalso fakePlayer = null;
        if (GerenciadorSkinNpc.usaCpm(npc.getId())) {
            fakePlayer = JogadorNpcFalso.getOrCreate(npc);
            if (fakePlayer == null) return;
            fakePlayer.setCustomSkinTexture(GerenciadorSkinNpc.getSkinTexture(npc.getId()));
        } else {
            JogadorNpcFalso.liberarCpm(npc);
        }

        boolean isSlim = "slim".equals(npc.getModelType());

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

        if (fakePlayer != null) {
            (isSlim ? slimRenderer : wideRenderer).render(fakePlayer, entityYaw, partialTick, poseStack, buffer, packedLight);
        } else {
            (isSlim ? corpoFino : corpoLargo).render(npc, entityYaw, partialTick, poseStack, buffer, packedLight);
        }

        if (hasTransform) {
            poseStack.popPose();
        }

        double distSq = this.entityRenderDispatcher.distanceToSqr(npc);
        if (npc.getNameVisible() && distSq < 32) {
            renderNpcNameTag(npc, poseStack, buffer, packedLight);
        }
    }

    private void renderNpcNameTag(EntidadeNpcPersonalizado npc, PoseStack poseStack,
                                  MultiBufferSource buffer, int packedLight) {
        String rawName = npc.getNpcName();
        if (rawName == null || rawName.isBlank()) return;

        Component displayName = npc.getDisplayName();
        Font font = Minecraft.getInstance().font;
        CacheNome cache = cacheNomes.get(npc);
        if (cache == null) {
            cache = new CacheNome();
            cacheNomes.put(npc, cache);
        }
        int versaoFonte = VersaoFonteCliente.atual();
        if (cache.componente != displayName || cache.versaoFonte != versaoFonte) {
            cache.componente = displayName;
            cache.versaoFonte = versaoFonte;
            cache.metadeLargura = font.width(displayName) / 2.0f;
        }

        float sy = npc.getScaleY();
        float nameHeight = (npc.getBbHeight() * sy) + 0.5f;

        poseStack.pushPose();
        poseStack.translate(0.0, nameHeight, 0.0);

        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.025f, -0.025f, 0.025f);

        float x = -cache.metadeLargura;

        Matrix4f matrix = poseStack.last().pose();

        int bgAlpha = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25f) * 255.0f);
        int bgColor = bgAlpha << 24;

        font.drawInBatch(displayName, x, 0, 0x20FFFFFF, false,
                matrix, buffer, Font.DisplayMode.SEE_THROUGH, bgColor, packedLight);

        font.drawInBatch(displayName, x, 0, -1, false,
                matrix, buffer, Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();
    }

    @Override
    protected void renderNameTag(EntidadeNpcPersonalizado entity, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {

    }

    @Override
    public ResourceLocation getTextureLocation(EntidadeNpcPersonalizado entity) {
        ResourceLocation customSkin = GerenciadorSkinNpc.getSkinTexture(entity.getId());
        if (customSkin != null) return customSkin;
        return "slim".equals(entity.getModelType())
                ? ResourceLocation.withDefaultNamespace("textures/entity/player/slim/alex.png")
                : ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
    }
}
