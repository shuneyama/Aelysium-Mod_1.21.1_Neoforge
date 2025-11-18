package net.aelysium.aelysiummod.mixin;

import net.aelysium.aelysiummod.system.LuaEstado;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    private static final ResourceLocation BLOOD_MOON =
            ResourceLocation.fromNamespaceAndPath("aelysiummod", "textures/environment/blood_moon.png");

    private static final ResourceLocation MOON_PHASES =
            ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");

    // Intercepta TODAS as chamadas de setShaderTexture e substitui se for a lua
    @ModifyArg(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"
            ),
            index = 1
    )
    private ResourceLocation replaceBloodMoonTexture(ResourceLocation original) {
        // Se for a textura da lua E a blood moon estiver ativa
        if (LuaEstado.bloodMoon && original.equals(MOON_PHASES)) {
            return BLOOD_MOON;
        }
        return original;
    }

    // Dobra o tamanho da lua (20F -> 40F)
    @ModifyConstant(method = "renderSky", constant = @Constant(floatValue = 20.0F))
    private float modifyMoonSize(float original) {
        if (LuaEstado.bloodMoon) {
            return 100.0F; // 2x maior
        }
        return original;
    }
}