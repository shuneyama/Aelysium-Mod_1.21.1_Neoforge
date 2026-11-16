package net.aelysium.aelysiummod.datagen;

import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.aelysium.aelysiummod.registro.ModBlocos;
import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class ProvedorTabelaLootBlocos extends BlockLootSubProvider {
    protected ProvedorTabelaLootBlocos(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {

        add(ModBlocos.CIANE_MINERIO.get(),
                block -> createOreDrop(ModBlocos.CIANE_MINERIO.get(), ModItens.CIANE_CRU.get()));
        add(ModBlocos.DAMA_DA_NOITE.get(),
                block -> LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(block))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(net.aelysium.aelysiummod.conteudo.bloco.DamaNoite.LUA_VERMELHA, false))))));

        add(ModBlocos.POTTED_DAMA_DA_NOITE.get(),
                block -> createPotFlowerItemTable(ModBlocos.DAMA_DA_NOITE.get()));

        add(ModBlocos.FENDA_KAIROS.get(),
                block -> createSingleItemTable(ModBlocos.FENDA_KAIROS.get()));

        add(ModBlocos.PORTA_UM_PAINEL.get(),
                block -> createDoorTable(ModBlocos.PORTA_UM_PAINEL.get()));

        add(ModBlocos.COFRE_COM_PAINEL.get(),
                block -> createSingleItemTable(ModBlocos.COFRE_COM_PAINEL.get()));

        dropSelf(ModBlocos.MINI_FAROL.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocos.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}