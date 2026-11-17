package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public abstract class MixinNuvensSincronizadas {

    @Shadow
    private int ticks;
    @Shadow
    private ClientLevel level;

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FDDD)V"))
    private void aelysium$nuvens(LevelRenderer self, PoseStack pose, Matrix4f frustum, Matrix4f projecao, float parcial, double x, double y, double z, Operation<Void> original) {
        int guardado = this.ticks;
        if (this.level != null) this.ticks = (int) (this.level.getGameTime() % 819200L);
        try {
            original.call(self, pose, frustum, projecao, parcial, x, y, z);
        } finally {
            this.ticks = guardado;
        }
    }
}
