package net.aelysium.aelysiummod.datagen;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.item.ModItens;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;


import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, AelysiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ItemTags.PICKAXES)
                .add(ModItens.CIANE_PICARETA.get());
        tag(ItemTags.AXES)
                .add(ModItens.CIANE_MACHADO.get());
        tag(ItemTags.SHOVELS)
                .add(ModItens.CIANE_PA.get());
        tag(ItemTags.HOES)
                .add(ModItens.CIANE_ENXADA.get());
        tag(ItemTags.SWORDS)
                .add(ModItens.CIANE_ESPADA.get())
                .add(ModItens.ESPADA_ESPECIAL.get())
                .add(ModItens.PARAR.get())
                .add(ModItens.CANO.get());
        tag(ItemTags.HEAD_ARMOR)
                .add(ModItens.CIANE_CAPACETE.get());
        tag(ItemTags.CHEST_ARMOR)
                .add(ModItens.CIANE_PEITORAL.get());
        tag(ItemTags.LEG_ARMOR)
                .add(ModItens.CIANE_CALCINHA.get());
        tag(ItemTags.FOOT_ARMOR)
                .add(ModItens.CIANE_BOTAS.get());
    }
}