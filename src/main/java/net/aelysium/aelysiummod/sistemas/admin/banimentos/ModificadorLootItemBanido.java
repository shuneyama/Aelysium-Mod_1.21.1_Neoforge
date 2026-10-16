package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoListaBanimentos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.Iterator;
import net.aelysium.aelysiummod.registro.ModModificadoresLoot;

public class ModificadorLootItemBanido extends LootModifier {

    private static final float REDUCAO = 0.8F;

    public static final MapCodec<ModificadorLootItemBanido> CODEC =
            RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance, ModificadorLootItemBanido::new));

    public ModificadorLootItemBanido(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (ConfiguracaoListaBanimentos.getBannedItems().isEmpty()) return generatedLoot;

        RandomSource random = context.getRandom();
        Iterator<ItemStack> iterator = generatedLoot.iterator();

        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

            if (ConfiguracaoListaBanimentos.isBanned(itemId) && random.nextFloat() < REDUCAO) {
                iterator.remove();
            }
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModModificadoresLoot.BANNED_ITEM_REDUCER.get();
    }
}
