package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.IRenderer;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.projetil.SporeBallProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public class SporeBallSizeRenderer implements IRenderer<SporeBallProjectile> {
    public SporeBallSizeRenderer(){}

    @Override
    public void render(
            SporeBallProjectile entity,
            float yaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light
    ) {
        float deathProgress = (!entity.impacted || entity.isRemoved()) ? 0f : ((entity.impactedTicks + partialTicks) / SporeBallProjectile.explosionDelay) * 0.5f;
        float scaledDeathProgress = (float) (Math.pow(deathProgress, 2f) + 1);
        poseStack.scale(scaledDeathProgress, scaledDeathProgress, scaledDeathProgress);
    }
}
