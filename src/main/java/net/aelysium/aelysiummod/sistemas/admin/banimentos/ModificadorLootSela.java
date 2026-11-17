package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.aelysium.aelysiummod.registro.ModModificadoresLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.Set;

public class ModificadorLootSela extends LootModifier {

    private static final Set<ResourceLocation> TABELAS = Set.of(
            ResourceLocation.withDefaultNamespace("chests/simple_dungeon"),
            ResourceLocation.withDefaultNamespace("chests/ancient_city"),
            ResourceLocation.withDefaultNamespace("chests/desert_pyramid"),
            ResourceLocation.withDefaultNamespace("chests/jungle_temple"),
            ResourceLocation.withDefaultNamespace("chests/stronghold_corridor")
    );

    public static final MapCodec<ModificadorLootSela> CODEC =
            RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance, ModificadorLootSela::new));

    public ModificadorLootSela(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (!TABELAS.contains(context.getQueriedLootTableId())) return loot;
        for (int i = 0; i < loot.size(); i++) {
            if (loot.get(i).is(Items.SADDLE)) {
                loot.set(i, new ItemStack(Items.LEATHER, 1 + context.getRandom().nextInt(5)));
            }
        }
        return loot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModModificadoresLoot.SADDLE_TO_LEATHER.get();
    }
}
