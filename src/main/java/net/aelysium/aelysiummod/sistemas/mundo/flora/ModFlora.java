package net.aelysium.aelysiummod.sistemas.mundo.flora;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModFlora {

    public static final DeferredRegister.Blocks BLOCOS = DeferredRegister.createBlocks(AelysiumMod.MOD_ID);
    public static final DeferredRegister.Items ITENS = DeferredRegister.createItems(AelysiumMod.MOD_ID);
    public static final DeferredRegister<ParticleType<?>> PARTICULAS = DeferredRegister.create(Registries.PARTICLE_TYPE, AelysiumMod.MOD_ID);

    public static final Supplier<SimpleParticleType> PARTICULA_VAGALUME = PARTICULAS.register("firefly", () -> new SimpleParticleType(false));

    public static final DeferredBlock<Block> ARBUSTO_VAGALUME = BLOCOS.register("firefly_bush",
            () -> new BlocosFlora.ArbustoVagalume(Properties.of().mapColor(MapColor.PLANT).ignitedByLava().lightLevel(estado -> 2)
                    .noCollission().instabreak().sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> FOLHAS_SECAS = BLOCOS.register("leaf_litter",
            () -> new BlocosFlora.FolhasSecas(Properties.of().mapColor(MapColor.COLOR_BROWN).replaceable().noCollission()
                    .sound(SonsExtras.LEAF_LITTER).pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> FLORES_SELVAGENS = BLOCOS.register("wildflowers",
            () -> new BlocosFlora.FlorSelvagem(Properties.of().mapColor(MapColor.PLANT).noCollission()
                    .sound(SoundType.PINK_PETALS).pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> ARBUSTO = BLOCOS.register("bush",
            () -> new BlocosFlora.Arbusto(Properties.of().mapColor(MapColor.PLANT).replaceable().noCollission().instabreak()
                    .sound(SoundType.GRASS).ignitedByLava().pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> GRAMA_SECA_BAIXA = BLOCOS.register("short_dry_grass",
            () -> new BlocosFlora.GramaSecaBaixa(Properties.of().mapColor(MapColor.COLOR_YELLOW).replaceable().noCollission().instabreak()
                    .sound(SoundType.GRASS).ignitedByLava().offsetType(OffsetType.XYZ).pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> GRAMA_SECA_ALTA = BLOCOS.register("tall_dry_grass",
            () -> new BlocosFlora.GramaSecaAlta(Properties.of().mapColor(MapColor.COLOR_YELLOW).replaceable().noCollission().instabreak()
                    .sound(SoundType.GRASS).ignitedByLava().offsetType(OffsetType.XYZ).pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> FLOR_DE_CACTO = BLOCOS.register("cactus_flower",
            () -> new BlocosFlora.FlorDeCacto(Properties.of().mapColor(MapColor.COLOR_PINK).noCollission().instabreak().ignitedByLava()
                    .sound(SonsExtras.CACTUS_FLOWER).pushReaction(PushReaction.DESTROY)));

    public static final DeferredItem<BlockItem> ITEM_ARBUSTO_VAGALUME = ITENS.registerSimpleBlockItem(ARBUSTO_VAGALUME);
    public static final DeferredItem<BlockItem> ITEM_FOLHAS_SECAS = ITENS.registerSimpleBlockItem(FOLHAS_SECAS);
    public static final DeferredItem<BlockItem> ITEM_FLORES_SELVAGENS = ITENS.registerSimpleBlockItem(FLORES_SELVAGENS);
    public static final DeferredItem<BlockItem> ITEM_ARBUSTO = ITENS.registerSimpleBlockItem(ARBUSTO);
    public static final DeferredItem<BlockItem> ITEM_GRAMA_SECA_BAIXA = ITENS.registerSimpleBlockItem(GRAMA_SECA_BAIXA);
    public static final DeferredItem<BlockItem> ITEM_GRAMA_SECA_ALTA = ITENS.registerSimpleBlockItem(GRAMA_SECA_ALTA);
    public static final DeferredItem<BlockItem> ITEM_FLOR_DE_CACTO = ITENS.registerSimpleBlockItem(FLOR_DE_CACTO);

    public static final DeferredItem<Item> OVO_AZUL = ITENS.register("blue_egg", () -> new net.minecraft.world.item.EggItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> OVO_MARROM = ITENS.register("brown_egg", () -> new net.minecraft.world.item.EggItem(new Item.Properties().stacksTo(16)));

    private ModFlora() {
    }

    public static void registrar(IEventBus barramento) {
        BLOCOS.register(barramento);
        ITENS.register(barramento);
        PARTICULAS.register(barramento);
    }

    @EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class Aba {
        @SubscribeEvent
        public static void aba(BuildCreativeModeTabContentsEvent evento) {
            if (evento.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                evento.accept(OVO_AZUL);
                evento.accept(OVO_MARROM);
            }
            if (evento.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
                evento.accept(ITEM_ARBUSTO_VAGALUME);
                evento.accept(ITEM_FOLHAS_SECAS);
                evento.accept(ITEM_FLORES_SELVAGENS);
                evento.accept(ITEM_ARBUSTO);
                evento.accept(ITEM_GRAMA_SECA_BAIXA);
                evento.accept(ITEM_GRAMA_SECA_ALTA);
                evento.accept(ITEM_FLOR_DE_CACTO);
                evento.accept(net.aelysium.aelysiummod.registro.ModBlocos.DENTE_DE_LEAO_DOURADO);
            }
        }
    }
}
