package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.IOverlayOverride;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.IRenderer;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.projetil.SporeBallProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class SporeBallOverlay implements IRenderer<SporeBallProjectile>, IOverlayOverride {
    private SporeBallProjectile entity;
    private float partialTicks;

    @Override
    public void render(
            SporeBallProjectile entity,
            float yaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light
    ) {
        this.entity = entity;
        this.partialTicks = partialTicks;
    }

    @Override
    public int getOverlay() {
        if (entity == null) return OverlayTexture.pack(OverlayTexture.u(0f), OverlayTexture.v(false));
        float partialTicks = (this.partialTicks != 0f) ? this.partialTicks : 0f;
        float deathProgress = (!entity.impacted || entity.isRemoved()) ? 0f : (entity.impactedTicks + partialTicks) / SporeBallProjectile.explosionDelay;
        float scaledDeathProgress = (float) Math.pow(deathProgress, 2f);
        return OverlayTexture.pack(OverlayTexture.u(scaledDeathProgress), OverlayTexture.v(false));
    }
}
