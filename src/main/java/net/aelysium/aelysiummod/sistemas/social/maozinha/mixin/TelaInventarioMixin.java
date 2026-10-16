package net.aelysium.aelysiummod.sistemas.social.maozinha.mixin;

import net.aelysium.aelysiummod.sistemas.social.maozinha.cliente.AnimacaoBracoMaozinha;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class TelaInventarioMixin {

    @Inject(method = "renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("HEAD"))
    private static void aelysium_entrarInterfaceMaozinha(GuiGraphics graficos, float x, float y, float escala, Vector3f translacao,
                                                         Quaternionf pose, Quaternionf camera, LivingEntity entidade, CallbackInfo ci) {
        AnimacaoBracoMaozinha.definirEmInterface(true);
    }

    @Inject(method = "renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("RETURN"))
    private static void aelysium_sairInterfaceMaozinha(GuiGraphics graficos, float x, float y, float escala, Vector3f translacao,
                                                       Quaternionf pose, Quaternionf camera, LivingEntity entidade, CallbackInfo ci) {
        AnimacaoBracoMaozinha.definirEmInterface(false);
    }
}
