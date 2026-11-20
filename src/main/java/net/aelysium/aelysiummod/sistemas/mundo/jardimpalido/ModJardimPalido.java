package net.aelysium.aelysiummod.sistemas.mundo.jardimpalido;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

public final class ModJardimPalido {

    public static final DeferredRegister.Blocks BLOCOS = DeferredRegister.createBlocks(AelysiumMod.MOD_ID);
    public static final DeferredRegister.Items ITENS = DeferredRegister.createItems(AelysiumMod.MOD_ID);
    public static final DeferredRegister<ParticleType<?>> PARTICULAS = DeferredRegister.create(Registries.PARTICLE_TYPE, AelysiumMod.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTIDADES = DeferredRegister.create(Registries.ENTITY_TYPE, AelysiumMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> TIPOS_BLOCO = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AelysiumMod.MOD_ID);

    public static final BlockSetType TIPO_CONJUNTO = BlockSetType.register(new BlockSetType("aelysiummod_pale_oak"));
    public static final WoodType TIPO_MADEIRA = WoodType.register(new WoodType("aelysiummod_pale_oak", TIPO_CONJUNTO));

    public static final ResourceKey<ConfiguredFeature<?, ?>> ARVORE = ResourceKey.create(Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "pale_oak"));
    public static final TreeGrower CRESCEDOR = new TreeGrower("aelysiummod_pale_oak", Optional.of(ARVORE), Optional.empty(), Optional.empty());

    public static final Supplier<SimpleParticleType> PARTICULA_FOLHAS = PARTICULAS.register("pale_oak_leaves", () -> new SimpleParticleType(false));

    private static Properties madeira() {
        return Properties.of().mapColor(MapColor.QUARTZ).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS)
                .strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava();
    }

    private static Properties tronco() {
        return Properties.of().mapColor(MapColor.QUARTZ).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS)
                .strength(2.0F).sound(SoundType.WOOD).ignitedByLava();
    }

    private static Properties resina() {
        return Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE);
    }

    public static final DeferredBlock<Block> TABUAS = BLOCOS.register("pale_oak_planks", () -> new Block(madeira()));
    public static final DeferredBlock<RotatedPillarBlock> TORA = BLOCOS.register("pale_oak_log", () -> new RotatedPillarBlock(tronco()));
    public static final DeferredBlock<RotatedPillarBlock> TORA_DESCASCADA = BLOCOS.register("stripped_pale_oak_log", () -> new RotatedPillarBlock(tronco()));
    public static final DeferredBlock<RotatedPillarBlock> MADEIRA = BLOCOS.register("pale_oak_wood", () -> new RotatedPillarBlock(tronco()));
    public static final DeferredBlock<RotatedPillarBlock> MADEIRA_DESCASCADA = BLOCOS.register("stripped_pale_oak_wood", () -> new RotatedPillarBlock(tronco()));
    public static final DeferredBlock<Block> FOLHAS = BLOCOS.register("pale_oak_leaves", () -> new BlocosJardimPalido.FolhasCarvalhoPalido(
            Properties.of().mapColor(MapColor.QUARTZ).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion()
                    .isValidSpawn((e, m, p, t) -> false).isSuffocating((e, m, p) -> false).isViewBlocking((e, m, p) -> false)
                    .ignitedByLava().pushReaction(PushReaction.DESTROY).isRedstoneConductor((e, m, p) -> false)));
    public static final DeferredBlock<Block> MUDA = BLOCOS.register("pale_oak_sapling", () -> new SaplingBlock(CRESCEDOR,
            Properties.of().mapColor(MapColor.QUARTZ).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> MUDA_VASO = BLOCOS.register("potted_pale_oak_sapling", () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT,
            MUDA, Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<SlabBlock> LAJE = BLOCOS.register("pale_oak_slab", () -> new SlabBlock(madeira()));
    public static final DeferredBlock<StairBlock> ESCADA = BLOCOS.register("pale_oak_stairs", () -> new StairBlock(TABUAS.get().defaultBlockState(), madeira()));
    public static final DeferredBlock<FenceBlock> CERCA = BLOCOS.register("pale_oak_fence", () -> new FenceBlock(madeira()));
    public static final DeferredBlock<FenceGateBlock> PORTEIRA = BLOCOS.register("pale_oak_fence_gate", () -> new FenceGateBlock(TIPO_MADEIRA, madeira()));
    public static final DeferredBlock<ButtonBlock> BOTAO = BLOCOS.register("pale_oak_button", () -> new ButtonBlock(TIPO_CONJUNTO, 30,
            Properties.of().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<PressurePlateBlock> PLACA_PRESSAO = BLOCOS.register("pale_oak_pressure_plate", () -> new PressurePlateBlock(TIPO_CONJUNTO,
            madeira().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<DoorBlock> PORTA = BLOCOS.register("pale_oak_door", () -> new DoorBlock(TIPO_CONJUNTO,
            madeira().strength(3.0F).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<TrapDoorBlock> ALCAPAO = BLOCOS.register("pale_oak_trapdoor", () -> new TrapDoorBlock(TIPO_CONJUNTO,
            madeira().strength(3.0F).noOcclusion().isValidSpawn((e, m, p, t) -> false)));

    public static final DeferredBlock<Block> BLOCO_RESINA = BLOCOS.register("resin_block", () -> new Block(
            Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).instabreak().sound(SoundType.STONE).pushReaction(PushReaction.NORMAL)));
    public static final DeferredBlock<Block> TIJOLOS_RESINA = BLOCOS.register("resin_bricks", () -> new Block(resina()));
    public static final DeferredBlock<Block> TIJOLOS_RESINA_CINZELADOS = BLOCOS.register("chiseled_resin_bricks", () -> new Block(resina()));
    public static final DeferredBlock<SlabBlock> LAJE_TIJOLOS_RESINA = BLOCOS.register("resin_brick_slab", () -> new SlabBlock(resina()));
    public static final DeferredBlock<StairBlock> ESCADA_TIJOLOS_RESINA = BLOCOS.register("resin_brick_stairs", () -> new StairBlock(TIJOLOS_RESINA.get().defaultBlockState(), resina()));
    public static final DeferredBlock<WallBlock> MURO_TIJOLOS_RESINA = BLOCOS.register("resin_brick_wall", () -> new WallBlock(resina()));
    public static final DeferredBlock<Block> GRUMO_RESINA = BLOCOS.register("resin_clump", () -> new BlocosJardimPalido.GrumoResina(
            Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).replaceable().noCollission().strength(0.0F).sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> MUSGO_PALIDO = BLOCOS.register("pale_moss_block", () -> new Block(
            Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(0.1F).sound(SoundType.MOSS).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> TAPETE_MUSGO_PALIDO = BLOCOS.register("pale_moss_carpet", () -> new net.minecraft.world.level.block.CarpetBlock(
            Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(0.1F).sound(SoundType.MOSS).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> MUSGO_PENDENTE = BLOCOS.register("pale_hanging_moss", () -> new BlocosJardimPalido.MusgoPendente(
            Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).replaceable().noCollission().strength(0.0F).sound(SoundType.MOSS_CARPET).pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> FLOR_OLHO_ABERTA = BLOCOS.register("open_eyeblossom", () -> new BlocosJardimPalido.FlorDeOlho(true,
            Properties.of().mapColor(MapColor.COLOR_ORANGE).noCollission().instabreak().sound(SoundType.GRASS).randomTicks().lightLevel(e -> 4)
                    .offsetType(net.minecraft.world.level.block.state.BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> FLOR_OLHO_FECHADA = BLOCOS.register("closed_eyeblossom", () -> new BlocosJardimPalido.FlorDeOlho(false,
            Properties.of().mapColor(MapColor.COLOR_GRAY).noCollission().instabreak().sound(SoundType.GRASS).randomTicks()
                    .offsetType(net.minecraft.world.level.block.state.BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> FLOR_OLHO_ABERTA_VASO = BLOCOS.register("potted_open_eyeblossom", () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT,
            FLOR_OLHO_ABERTA, Properties.of().instabreak().noOcclusion().lightLevel(e -> 4).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> FLOR_OLHO_FECHADA_VASO = BLOCOS.register("potted_closed_eyeblossom", () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT,
            FLOR_OLHO_FECHADA, Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

    public static final DeferredItem<BlockItem> ITEM_TABUAS = ITENS.registerSimpleBlockItem(TABUAS);
    public static final DeferredItem<BlockItem> ITEM_TORA = ITENS.registerSimpleBlockItem(TORA);
    public static final DeferredItem<BlockItem> ITEM_TORA_DESCASCADA = ITENS.registerSimpleBlockItem(TORA_DESCASCADA);
    public static final DeferredItem<BlockItem> ITEM_MADEIRA = ITENS.registerSimpleBlockItem(MADEIRA);
    public static final DeferredItem<BlockItem> ITEM_MADEIRA_DESCASCADA = ITENS.registerSimpleBlockItem(MADEIRA_DESCASCADA);
    public static final DeferredItem<BlockItem> ITEM_FOLHAS = ITENS.registerSimpleBlockItem(FOLHAS);
    public static final DeferredItem<BlockItem> ITEM_MUDA = ITENS.registerSimpleBlockItem(MUDA);
    public static final DeferredItem<BlockItem> ITEM_LAJE = ITENS.registerSimpleBlockItem(LAJE);
    public static final DeferredItem<BlockItem> ITEM_ESCADA = ITENS.registerSimpleBlockItem(ESCADA);
    public static final DeferredItem<BlockItem> ITEM_CERCA = ITENS.registerSimpleBlockItem(CERCA);
    public static final DeferredItem<BlockItem> ITEM_PORTEIRA = ITENS.registerSimpleBlockItem(PORTEIRA);
    public static final DeferredItem<BlockItem> ITEM_BOTAO = ITENS.registerSimpleBlockItem(BOTAO);
    public static final DeferredItem<BlockItem> ITEM_PLACA_PRESSAO = ITENS.registerSimpleBlockItem(PLACA_PRESSAO);
    public static final DeferredItem<BlockItem> ITEM_PORTA = ITENS.registerSimpleBlockItem(PORTA);
    public static final DeferredItem<BlockItem> ITEM_ALCAPAO = ITENS.registerSimpleBlockItem(ALCAPAO);
    public static final DeferredItem<BlockItem> ITEM_BLOCO_RESINA = ITENS.registerSimpleBlockItem(BLOCO_RESINA);
    public static final DeferredItem<BlockItem> ITEM_TIJOLOS_RESINA = ITENS.registerSimpleBlockItem(TIJOLOS_RESINA);
    public static final DeferredItem<BlockItem> ITEM_TIJOLOS_RESINA_CINZELADOS = ITENS.registerSimpleBlockItem(TIJOLOS_RESINA_CINZELADOS);
    public static final DeferredItem<BlockItem> ITEM_LAJE_TIJOLOS_RESINA = ITENS.registerSimpleBlockItem(LAJE_TIJOLOS_RESINA);
    public static final DeferredItem<BlockItem> ITEM_ESCADA_TIJOLOS_RESINA = ITENS.registerSimpleBlockItem(ESCADA_TIJOLOS_RESINA);
    public static final DeferredItem<BlockItem> ITEM_MURO_TIJOLOS_RESINA = ITENS.registerSimpleBlockItem(MURO_TIJOLOS_RESINA);
    public static final DeferredItem<BlockItem> ITEM_GRUMO_RESINA = ITENS.registerSimpleBlockItem(GRUMO_RESINA);
    public static final DeferredItem<BlockItem> ITEM_MUSGO_PALIDO = ITENS.registerSimpleBlockItem(MUSGO_PALIDO);
    public static final DeferredItem<BlockItem> ITEM_TAPETE_MUSGO_PALIDO = ITENS.registerSimpleBlockItem(TAPETE_MUSGO_PALIDO);
    public static final DeferredItem<BlockItem> ITEM_MUSGO_PENDENTE = ITENS.registerSimpleBlockItem(MUSGO_PENDENTE);
    public static final DeferredItem<BlockItem> ITEM_FLOR_OLHO_ABERTA = ITENS.registerSimpleBlockItem(FLOR_OLHO_ABERTA);
    public static final DeferredItem<BlockItem> ITEM_FLOR_OLHO_FECHADA = ITENS.registerSimpleBlockItem(FLOR_OLHO_FECHADA);
    public static final DeferredItem<Item> TIJOLO_RESINA = ITENS.registerSimpleItem("resin_brick");

    public static final Supplier<EntityType<Creaking>> CREAKING = ENTIDADES.register("creaking", () -> EntityType.Builder.of(Creaking::new, MobCategory.MONSTER)
            .sized(0.9F, 2.7F).eyeHeight(2.3F).clientTrackingRange(8).build("creaking"));
    public static final DeferredBlock<Block> CORACAO = BLOCOS.register("creaking_heart", () -> new CoracaoDoCreaking(
            Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM)
                    .strength(5.0F).sound(SonsJardimPalido.TIPO_CORACAO)));
    public static final DeferredItem<BlockItem> ITEM_CORACAO = ITENS.registerSimpleBlockItem(CORACAO);
    public static final Supplier<BlockEntityType<CoracaoDoCreakingEntidade>> TIPO_CORACAO = TIPOS_BLOCO.register("creaking_heart",
            () -> BlockEntityType.Builder.of(CoracaoDoCreakingEntidade::new, CORACAO.get()).build(null));
    public static final DeferredItem<Item> OVO_CREAKING = ITENS.register("creaking_spawn_egg",
            () -> new DeferredSpawnEggItem(CREAKING, 0x5F5F5F, 0xFC7812, new Item.Properties()));

    private ModJardimPalido() {
    }

    public static void registrar(IEventBus barramento) {
        BLOCOS.register(barramento);
        ITENS.register(barramento);
        PARTICULAS.register(barramento);
        ENTIDADES.register(barramento);
        TIPOS_BLOCO.register(barramento);
        SonsJardimPalido.registrar(barramento);
        barramento.addListener(ModJardimPalido::atributos);
    }

    private static void atributos(EntityAttributeCreationEvent evento) {
        evento.put(CREAKING.get(), Creaking.atributos().build());
    }

    public static void configuracaoComum() {
        FlowerPotBlock vaso = (FlowerPotBlock) Blocks.FLOWER_POT;
        vaso.addPlant(MUDA.getId(), MUDA_VASO);
        vaso.addPlant(FLOR_OLHO_ABERTA.getId(), FLOR_OLHO_ABERTA_VASO);
        vaso.addPlant(FLOR_OLHO_FECHADA.getId(), FLOR_OLHO_FECHADA_VASO);
        FireBlock fogo = (FireBlock) Blocks.FIRE;
        for (Supplier<? extends Block> b : new Supplier[]{TABUAS, LAJE, ESCADA, CERCA, PORTEIRA}) fogo.setFlammable(b.get(), 5, 20);
        for (Supplier<? extends Block> b : new Supplier[]{TORA, TORA_DESCASCADA, MADEIRA, MADEIRA_DESCASCADA}) fogo.setFlammable(b.get(), 5, 5);
        fogo.setFlammable(FOLHAS.get(), 30, 60);
        fogo.setFlammable(TAPETE_MUSGO_PALIDO.get(), 60, 20);
        fogo.setFlammable(MUSGO_PENDENTE.get(), 30, 60);
        fogo.setFlammable(FLOR_OLHO_ABERTA.get(), 60, 100);
        fogo.setFlammable(FLOR_OLHO_FECHADA.get(), 60, 100);
    }

    @EventBusSubscriber(modid = AelysiumMod.MOD_ID)
    public static class Aba {
        @SubscribeEvent
        public static void aba(BuildCreativeModeTabContentsEvent evento) {
            if (evento.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
                evento.accept(ITEM_TORA);
                evento.accept(ITEM_MADEIRA);
                evento.accept(ITEM_TORA_DESCASCADA);
                evento.accept(ITEM_MADEIRA_DESCASCADA);
                evento.accept(ITEM_TABUAS);
                evento.accept(ITEM_LAJE);
                evento.accept(ITEM_ESCADA);
                evento.accept(ITEM_CERCA);
                evento.accept(ITEM_PORTEIRA);
                evento.accept(ITEM_PORTA);
                evento.accept(ITEM_ALCAPAO);
                evento.accept(ITEM_BOTAO);
                evento.accept(ITEM_PLACA_PRESSAO);
                evento.accept(ITEM_BLOCO_RESINA);
                evento.accept(ITEM_TIJOLOS_RESINA);
                evento.accept(ITEM_ESCADA_TIJOLOS_RESINA);
                evento.accept(ITEM_LAJE_TIJOLOS_RESINA);
                evento.accept(ITEM_MURO_TIJOLOS_RESINA);
                evento.accept(ITEM_TIJOLOS_RESINA_CINZELADOS);
            }
            if (evento.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
                evento.accept(ITEM_PORTA);
                evento.accept(ITEM_ALCAPAO);
                evento.accept(ITEM_PORTEIRA);
                evento.accept(ITEM_BOTAO);
                evento.accept(ITEM_PLACA_PRESSAO);
            }
            if (evento.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
                evento.accept(ITEM_TORA);
                evento.accept(ITEM_FOLHAS);
                evento.accept(ITEM_MUDA);
                evento.accept(ITEM_FLOR_OLHO_FECHADA);
                evento.accept(ITEM_FLOR_OLHO_ABERTA);
                evento.accept(ITEM_MUSGO_PALIDO);
                evento.accept(ITEM_TAPETE_MUSGO_PALIDO);
                evento.accept(ITEM_MUSGO_PENDENTE);
                evento.accept(ITEM_CORACAO);
                evento.accept(ITEM_GRUMO_RESINA);
            }
            if (evento.getTabKey() == CreativeModeTabs.SPAWN_EGGS) evento.accept(OVO_CREAKING);
            if (evento.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) evento.accept(ITEM_CORACAO);
            if (evento.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                evento.accept(ITEM_GRUMO_RESINA);
                evento.accept(TIJOLO_RESINA);
            }
        }
    }
}
