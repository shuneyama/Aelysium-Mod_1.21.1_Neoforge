package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public abstract class MixinBrilhoEscudo {

    @Redirect(method = "renderByItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getFoilBufferDirect(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 0))
    private VertexConsumer aelysium$brilhoReduzido(MultiBufferSource buffer, RenderType tipo, boolean semEntidade, boolean brilho) {
        return ItemRenderer.getArmorFoilBuffer(buffer, tipo, brilho);
    }
}
