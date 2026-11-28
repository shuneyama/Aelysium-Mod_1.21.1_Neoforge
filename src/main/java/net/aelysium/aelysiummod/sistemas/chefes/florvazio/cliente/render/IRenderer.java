package net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;

public interface IRenderer<T extends Entity> {
    void render(
            T entity,
            float yaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light
    );
}
