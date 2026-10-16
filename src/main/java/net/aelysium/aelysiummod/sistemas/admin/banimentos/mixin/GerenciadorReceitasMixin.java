package net.aelysium.aelysiummod.sistemas.admin.banimentos.mixin;

import net.aelysium.aelysiummod.sistemas.admin.banimentos.GerenciadorFiltroReceitas;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultContainer.class)
public class GerenciadorReceitasMixin {

    @Inject(method = "setItem", at = @At("HEAD"), cancellable = true)
    private void aelysium$filterCraftOutput(int slot, ItemStack stack, CallbackInfo ci) {
        if (!stack.isEmpty() && GerenciadorFiltroReceitas.isRecipeResultBlocked(stack)) {
            ci.cancel();
        }
    }
}
