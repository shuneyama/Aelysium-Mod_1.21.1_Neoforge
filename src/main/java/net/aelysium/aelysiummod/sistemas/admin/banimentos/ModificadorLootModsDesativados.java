package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.aelysium.aelysiummod.registro.ModModificadoresLoot;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoModsDesativados;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.Iterator;

public class ModificadorLootModsDesativados extends LootModifier {

    public static final MapCodec<ModificadorLootModsDesativados> CODEC =
            RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance, ModificadorLootModsDesativados::new));

    public ModificadorLootModsDesativados(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        Iterator<ItemStack> iterator = generatedLoot.iterator();

        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

            if (itemId != null && ConfiguracaoModsDesativados.isLootCapped(itemId.getNamespace())) {
                iterator.remove();
            }
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModModificadoresLoot.MOD_CAP_LOOT_REDUCER.get();
    }
}
