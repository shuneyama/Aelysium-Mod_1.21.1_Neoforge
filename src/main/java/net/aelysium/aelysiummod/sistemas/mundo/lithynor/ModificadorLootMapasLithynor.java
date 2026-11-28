package net.aelysium.aelysiummod.sistemas.mundo.lithynor;

import com.mojang.serialization.Codec;
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

public class ModificadorLootMapasLithynor extends LootModifier {

    private static final ResourceLocation LITHYNOR = ResourceLocation.fromNamespaceAndPath("aelysium", "lithynor");

    public static final MapCodec<ModificadorLootMapasLithynor> CODEC =
            RecordCodecBuilder.mapCodec(instance -> codecStart(instance)
                    .and(Codec.FLOAT.fieldOf("chance_remover").forGetter(m -> m.chanceRemover))
                    .apply(instance, ModificadorLootMapasLithynor::new));

    private final float chanceRemover;

    public ModificadorLootMapasLithynor(LootItemCondition[] conditionsIn, float chanceRemover) {
        super(conditionsIn);
        this.chanceRemover = chanceRemover;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (!context.getLevel().dimension().location().equals(LITHYNOR)) return loot;
        if (!context.getQueriedLootTableId().getPath().startsWith("chests/")) return loot;
        loot.removeIf(item -> item.is(Items.FILLED_MAP) && context.getRandom().nextFloat() < chanceRemover);
        return loot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModModificadoresLoot.MAPAS_TESOURO_LITHYNOR.get();
    }
}
