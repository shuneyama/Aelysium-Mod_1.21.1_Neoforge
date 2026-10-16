package net.aelysium.aelysiummod.sistemas.social.npc.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MerchantOffer.class)
public class OfertaTrocaMixin {

    @Redirect(
            method = "getModifiedCostCount",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I")
    )
    private int aelysium$limiteCusto(ItemStack custo) {
        return Math.max(custo.getMaxStackSize(), custo.getCount());
    }
}
