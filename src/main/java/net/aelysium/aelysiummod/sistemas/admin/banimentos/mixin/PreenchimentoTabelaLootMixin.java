package net.aelysium.aelysiummod.sistemas.admin.banimentos.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoModsDesativados;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Iterator;

@Mixin(LootTable.class)
public abstract class PreenchimentoTabelaLootMixin {

    @ModifyVariable(method = "fill", at = @At("STORE"), ordinal = 0)
    private ObjectArrayList<ItemStack> aelysium$filtrarLootCapado(ObjectArrayList<ItemStack> itens) {
        if (!AelysiumGuarda.isAutorizado()) return itens;

        Iterator<ItemStack> iterator = itens.iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id != null && ConfiguracaoModsDesativados.isLootCapped(id.getNamespace())) {
                iterator.remove();
            }
        }
        return itens;
    }
}
