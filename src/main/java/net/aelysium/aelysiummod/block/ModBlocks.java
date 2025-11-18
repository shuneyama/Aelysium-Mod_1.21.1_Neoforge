package net.aelysium.aelysiummod.block;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.block.custom.DamaNoite;
import net.aelysium.aelysiummod.block.custom.DamaNoiteVermelha;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.*;
import net.aelysium.aelysiummod.item.ModItens;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AelysiumMod.MOD_ID);

// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=





    public static final DeferredBlock<Block> BLOCO_AR = registerBlock("bloco_ar",
            () -> new BarrierBlock(BlockBehaviour.Properties.of()
                    .noOcclusion().noLootTable().strength(-1.0F, 3600000.8F)
                    .pushReaction(PushReaction.BLOCK).noTerrainParticles()));

    public static final DeferredBlock<Block> CIANE_MINERIO = registerBlock("ciane_minerio",
            () -> new DropExperienceBlock(UniformInt.of(5, 10),
                    BlockBehaviour.Properties.of().strength(7f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> DAMA_DA_NOITE = registerBlock("dama_da_noite",
            () -> new DamaNoite(MobEffects.WITHER, 0.35F, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> DAMA_VERMELHA_DA_NOITE = registerBlock("dama_vermelha_da_noite",
            () -> new DamaNoiteVermelha(MobEffects.HEALTH_BOOST, 0.35F, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));





// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItens.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}