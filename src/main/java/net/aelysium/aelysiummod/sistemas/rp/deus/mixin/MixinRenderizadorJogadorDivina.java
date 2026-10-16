package net.aelysium.aelysiummod.sistemas.rp.deus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.sistemas.rp.deus.cliente.CacheFormaDivina;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class MixinRenderizadorJogadorDivina {

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true)
    private void aelysium_cancelRenderFormaDivina(AbstractClientPlayer player, float entityYaw, float partialTick,
                                                  PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                                  CallbackInfo ci) {
        if (CacheFormaDivina.isActive(player.getUUID())) {
            ci.cancel();
        }
    }
}