package net.aelysium.aelysiummod.mixin;

import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.MeshData;
import net.aelysium.aelysiummod.efeitos.ModEfeitos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public class GlitchSkyMixin {

    @Redirect(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V",
                    ordinal = 0
            )
    )
    private void aelysium$cancelSunRenderIfGlitch(MeshData meshData) {
        Player player = Minecraft.getInstance().player;
        boolean hasGlitch = player != null &&
                (player.hasEffect(ModEfeitos.CEGUEIRA_ABISSAL) || player.hasEffect(ModEfeitos.CAECITAS));

        if (!hasGlitch) {
            BufferUploader.drawWithShader(meshData);
        }
    }
}