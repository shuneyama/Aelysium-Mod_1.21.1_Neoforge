package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Raid.class)
public abstract class MixinRaid {

    @Inject(method = "getLeaderBannerInstance", at = @At("RETURN"))
    private static void aelysium$raridadeEstandarte(CallbackInfoReturnable<ItemStack> cir) {
        cir.getReturnValue().set(DataComponents.RARITY, Rarity.UNCOMMON);
    }
}
