package net.aelysium.aelysiummod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.aelysium.aelysiummod.client.LuaClientRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LuaSkyMixin {

    @Inject(method = "renderSky", at = @At("HEAD"))
    private void aelysium$updateTransition(Matrix4f frustumMatrix, Matrix4f projectionMatrix,
                                           float partialTick, Camera camera,
                                           boolean isFoggy, Runnable setupFog,
                                           CallbackInfo ci) {
        LuaClientRenderer.updateTransition();
    }

    @Inject(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V",
                    ordinal = 1
            )
    )
    private void aelysium$beforeMoonRender(Matrix4f frustumMatrix, Matrix4f projectionMatrix,
                                           float partialTick, Camera camera,
                                           boolean isFoggy, Runnable setupFog,
                                           CallbackInfo ci) {
        float[] cor = LuaClientRenderer.getCorLua();
        RenderSystem.setShaderColor(cor[0], cor[1], cor[2], 1.0f);
    }

    @Inject(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V",
                    ordinal = 1
            )
    )
    private void aelysium$afterMoonRender(Matrix4f frustumMatrix, Matrix4f projectionMatrix,
                                          float partialTick, Camera camera,
                                          boolean isFoggy, Runnable setupFog,
                                          CallbackInfo ci) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @ModifyConstant(method = "renderSky", constant = @Constant(floatValue = 20.0F))
    private float aelysium$modifyMoonSize(float original) {
        if (LuaClientRenderer.transitionProgress > 0.0f) {
            return LuaClientRenderer.getCurrentSize();
        }
        return original;
    }
}
