package net.aelysium.aelysiummod.sistemas.mundo.lithynor.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.aelysium.aelysiummod.sistemas.mundo.lithynor.RestricoesLithynor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootTable.class)
public abstract class MixinTabelaLootLithynor {

    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"))
    private void aelysium$removerLootExcluidoLithynor(LootContext contexto, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        if (!RestricoesLithynor.ehLithynor(contexto.getLevel())) return;
        if (contexto.getQueriedLootTableId().getPath().startsWith("blocks/")) return;
        cir.getReturnValue().removeIf(item -> RestricoesLithynor.modExcluido(BuiltInRegistries.ITEM.getKey(item.getItem()).getNamespace()));
    }
}
