package net.aelysium.aelysiummod.datagen;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModBlocos;
import net.aelysium.aelysiummod.registro.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ProvedorTagsBlocos extends BlockTagsProvider {
    public ProvedorTagsBlocos(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, AelysiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocos.CIANE_MINERIO.get());

        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocos.CIANE_MINERIO.get());

        tag(ModTags.Blocos.NEEDS_CIANE_TOOL)
                .addTag(BlockTags.NEEDS_DIAMOND_TOOL);

        tag(ModTags.Blocos.INCORRECT_FOR_CIANE_TOOL)
                .addTag(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
                .remove(ModTags.Blocos.NEEDS_CIANE_TOOL);

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocos.PORTA_UM_PAINEL.get());
    }
}