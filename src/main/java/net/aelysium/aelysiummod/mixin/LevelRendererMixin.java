package net.aelysium.aelysiummod.mixin;

import net.aelysium.aelysiummod.eventos.LuaEstado;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    @Unique
    private static final ResourceLocation BLOOD_MOON =
            ResourceLocation.fromNamespaceAndPath("aelysiummod", "textures/environment/blood_moon.png");

    @Unique
    private static final ResourceLocation MOON_PHASES =
            ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");

    @Inject(method = "renderSky", at = @At("HEAD"))
    private void updateMoonTransition(CallbackInfo ci) {
        LuaEstado.updateTransition();
    }

    @ModifyArg(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"
            ),
            index = 1
    )
    private ResourceLocation replaceBloodMoonTexture(ResourceLocation original) {
        if (original.equals(MOON_PHASES) && LuaEstado.transitionProgress >= 1.0F) {
            return BLOOD_MOON;
        }
        return original;
    }

    @ModifyConstant(method = "renderSky", constant = @Constant(floatValue = 20.0F))
    private float modifyMoonSize(float original) {
        if (LuaEstado.transitionProgress > 0.0F) {
            return LuaEstado.getCurrentSize();
        }
        return original;
    }
}