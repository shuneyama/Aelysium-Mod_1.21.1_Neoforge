package net.aelysium.aelysiummod.sistemas.rp.raca.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(ItemInHandLayer.class)
public class BateriaItemMaoMixin {

    private static final float GIRO_Y = -45.0F;

    @Inject(method = "renderArmWithItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void aelysium$girarBateria(LivingEntity entity, ItemStack stack, ItemDisplayContext contexto, HumanoidArm braco,
                                       PoseStack poseStack, MultiBufferSource buffer, int luz, CallbackInfo ci) {
        if (!stack.is(ModItens.BATERIA.get()) || !entity.isUsingItem() || !entity.getUseItem().is(ModItens.BATERIA.get())) return;
        float progresso = Mth.clamp(entity.getTicksUsingItem() / 6.0F, 0.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(GIRO_Y * progresso));
    }
}
