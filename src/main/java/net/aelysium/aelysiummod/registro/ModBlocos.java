package net.aelysium.aelysiummod.registro;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.bloco.DamaNoite;
import net.aelysium.aelysiummod.conteudo.bloco.FendaKairos;
import net.aelysium.aelysiummod.sistemas.mundo.porta.BlocoPortaSenha;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.*;
import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocos {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AelysiumMod.MOD_ID);







    public static final java.util.List<DeferredBlock<Block>> MANCHAS_SANGUE = registrarManchasSangue(12);

    private static java.util.List<DeferredBlock<Block>> registrarManchasSangue(int quantidade) {
        java.util.List<DeferredBlock<Block>> lista = new java.util.ArrayList<>();
        for (int i = 1; i <= quantidade; i++) {
            lista.add(registerBlock("sangue_" + i,
                    () -> new net.aelysium.aelysiummod.conteudo.bloco.BlocoSangue(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_RED)
                            .noCollission()
                            .noOcclusion()
                            .replaceable()
                            .noLootTable()
                            .instabreak()
                            .sound(SoundType.WET_GRASS)
                            .pushReaction(PushReaction.DESTROY))));
        }
        return lista;
    }

    public static final java.util.List<DeferredBlock<Block>> MANCHAS_SANGUE_GRANDES = registrarManchasSangueGrandes(12);

    private static java.util.List<DeferredBlock<Block>> registrarManchasSangueGrandes(int quantidade) {
        java.util.List<DeferredBlock<Block>> lista = new java.util.ArrayList<>();
        for (int i = 1; i <= quantidade; i++) {
            lista.add(registerBlock("sangue_2x2_" + i,
                    () -> new net.aelysium.aelysiummod.conteudo.bloco.BlocoSangueGrande(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_RED)
                            .noCollission()
                            .noOcclusion()
                            .replaceable()
                            .noLootTable()
                            .instabreak()
                            .sound(SoundType.WET_GRASS)
                            .pushReaction(PushReaction.DESTROY))));
        }
        return lista;
    }

    public static final DeferredBlock<Block> BLOCO_AR = registerBlock("bloco_ar",
            () -> new BarrierBlock(BlockBehaviour.Properties.of()
                    .noOcclusion().noLootTable().strength(-1.0F, 3600000.8F)
                    .pushReaction(PushReaction.BLOCK).noTerrainParticles()));

    public static final DeferredBlock<Block> CIANE_MINERIO = registerBlock("ciane_minerio",
            () -> new DropExperienceBlock(UniformInt.of(5, 10),
                    BlockBehaviour.Properties.of().strength(7f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> DAMA_DA_NOITE = registerBlockComItem("dama_da_noite",
            () -> new DamaNoite(MobEffects.WITHER, 0.35F, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> POTTED_DAMA_DA_NOITE = BLOCKS.register("potted_dama_da_noite",
            () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, DAMA_DA_NOITE,
                    BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<net.aelysium.aelysiummod.sistemas.mundo.dificuldade.BlocoTeiaDificuldade> TEIA_DIFICULDADE = BLOCKS.register("teia_dificuldade",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.dificuldade.BlocoTeiaDificuldade(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .sound(SoundType.COBWEB)
                    .forceSolidOn()
                    .noCollission()
                    .noOcclusion()
                    .noLootTable()
                    .strength(0.4F)
                    .pushReaction(PushReaction.DESTROY)));

    public static final SoundType FENDA_KAIROS_SOUND = new SoundType(1.0F, 1.0F,
            SoundEvents.ENDERMAN_TELEPORT,
            SoundType.GLASS.getStepSound(),
            SoundEvents.ENDERMAN_TELEPORT,
            SoundType.GLASS.getHitSound(),
            SoundType.GLASS.getFallSound());

    public static final DeferredBlock<Block> FENDA_KAIROS = registerBlock("fenda_kairos",
            () -> new FendaKairos(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .noCollission()
                    .noOcclusion()
                    .strength(0.5F)
                    .sound(FENDA_KAIROS_SOUND)
                    .lightLevel(state -> 15)
                    .pushReaction(PushReaction.DESTROY)));






    public static final DeferredBlock<BlocoPortaSenha> PORTA_UM_PAINEL = registerDoorBlock("porta_um_painel",
            () -> new BlocoPortaSenha(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(-1.0F, 3600000.8F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)));

    public static final DeferredBlock<net.aelysium.aelysiummod.sistemas.mundo.porta.cofre.BlocoCofreComPainel> COFRE_COM_PAINEL = registerBlock("cofre_com_painel",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.cofre.BlocoCofreComPainel(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(-1.0F, 3600000.8F)
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)));

    public static final DeferredBlock<net.aelysium.aelysiummod.sistemas.mundo.portal.BlocoPortalLar> LAR_PORTAL = BLOCKS.register("lar_portal",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.portal.BlocoPortalLar(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .noCollission()
                    .noOcclusion()
                    .noLootTable()
                    .strength(-1.0F, 3600000.8F)
                    .lightLevel(state -> 11)
                    .sound(SoundType.GLASS)
                    .pushReaction(PushReaction.DESTROY)));

    private static <T extends Block> DeferredBlock<T> registerDoorBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        ModItens.ITEMS.register(name, () -> new DoubleHighBlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }

    private static DeferredBlock<Block> registerBlockComItem(String name, Supplier<Block> block) {
        DeferredBlock<Block> registrado = BLOCKS.register(name, block);
        ModItens.ITEMS.register(name, () -> new net.aelysium.aelysiummod.conteudo.bloco.DamaNoiteItem(registrado.get(), new Item.Properties()));
        return registrado;
    }

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