package net.aelysium.aelysiummod.datagen;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.mundo.jardimpalido.ModJardimPalido;
import net.aelysium.aelysiummod.registro.ModBlocos;
import net.aelysium.aelysiummod.registro.ModBlocosDecoracao;
import net.aelysium.aelysiummod.registro.ModTags;
import net.minecraft.world.level.block.Block;
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
                .add(ModBlocos.CIANE_MINERIO.get())
                .add(ModBlocos.MINI_FAROL.get());

        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocos.CIANE_MINERIO.get())
                .add(ModBlocos.MINI_FAROL.get());

        tag(ModTags.Blocos.NEEDS_CIANE_TOOL)
                .addTag(BlockTags.NEEDS_DIAMOND_TOOL);

        tag(ModTags.Blocos.INCORRECT_FOR_CIANE_TOOL)
                .addTag(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
                .remove(ModTags.Blocos.NEEDS_CIANE_TOOL);

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocos.PORTA_UM_PAINEL.get());

        for (ModBlocosDecoracao.Entrada entrada : ModBlocosDecoracao.ENTRADAS.values()) {
            Block bloco = entrada.bloco().get();
            switch (entrada.material().ferramenta()) {
                case PICARETA -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(bloco);
                case MACHADO -> tag(BlockTags.MINEABLE_WITH_AXE).add(bloco);
                case ENXADA -> tag(BlockTags.MINEABLE_WITH_HOE).add(bloco);
                default -> {
                }
            }
            switch (entrada.categoria()) {
                case LAJE -> tag(BlockTags.SLABS).add(bloco);
                case ESCADA -> tag(BlockTags.STAIRS).add(bloco);
                case MURO -> tag(BlockTags.WALLS).add(bloco);
                case PORTA -> tag(BlockTags.DOORS).add(bloco);
                case ALCAPAO -> tag(BlockTags.TRAPDOORS).add(bloco);
                default -> {
                }
            }
        }

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModJardimPalido.TABUAS.get(), ModJardimPalido.TORA.get(), ModJardimPalido.TORA_DESCASCADA.get(),
                        ModJardimPalido.MADEIRA.get(), ModJardimPalido.MADEIRA_DESCASCADA.get(), ModJardimPalido.LAJE.get(),
                        ModJardimPalido.ESCADA.get(), ModJardimPalido.CERCA.get(), ModJardimPalido.PORTEIRA.get(),
                        ModJardimPalido.BOTAO.get(), ModJardimPalido.PLACA_PRESSAO.get(), ModJardimPalido.PORTA.get(),
                        ModJardimPalido.ALCAPAO.get(), ModJardimPalido.CORACAO.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModJardimPalido.BLOCO_RESINA.get(), ModJardimPalido.TIJOLOS_RESINA.get(), ModJardimPalido.TIJOLOS_RESINA_CINZELADOS.get(),
                        ModJardimPalido.LAJE_TIJOLOS_RESINA.get(), ModJardimPalido.ESCADA_TIJOLOS_RESINA.get(), ModJardimPalido.MURO_TIJOLOS_RESINA.get());
        tag(BlockTags.MINEABLE_WITH_HOE)
                .add(ModJardimPalido.FOLHAS.get(), ModJardimPalido.MUSGO_PALIDO.get(), ModJardimPalido.TAPETE_MUSGO_PALIDO.get(), ModJardimPalido.MUSGO_PENDENTE.get());
        tag(BlockTags.SLABS).add(ModJardimPalido.LAJE_TIJOLOS_RESINA.get());
        tag(BlockTags.STAIRS).add(ModJardimPalido.ESCADA_TIJOLOS_RESINA.get());
        tag(BlockTags.WALLS).add(ModJardimPalido.MURO_TIJOLOS_RESINA.get());
    }
}