package net.aelysium.aelysiummod.sistemas.chefes.florvazio;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.chefes.base.particula.TipoParticulaSimples;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.bloco.MusgoFofoBlock;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.bloco.MusgoVazioBlock;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.bloco.VineWallBlock;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.bloco.VoidBlossomBlock;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.VoidBlossomEntity;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.projetil.PetalBladeProjectile;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.projetil.SporeBallProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class RegistroFlorVazio {

    public static final double VIDA = 750.0;
    public static final double ARMADURA = 4.0;
    public static final double ATAQUE = 13.0;
    public static final int EXPERIENCIA = 1000;
    public static final int BRILHO_TOTAL = 15728880;

    private static final DeferredRegister<EntityType<?>> ENTIDADES = DeferredRegister.create(Registries.ENTITY_TYPE, AelysiumMod.MOD_ID);
    private static final DeferredRegister.Blocks BLOCOS = DeferredRegister.createBlocks(AelysiumMod.MOD_ID);
    private static final DeferredRegister.Items ITENS = DeferredRegister.createItems(AelysiumMod.MOD_ID);
    private static final DeferredRegister<SoundEvent> SONS = DeferredRegister.create(Registries.SOUND_EVENT, AelysiumMod.MOD_ID);
    private static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICULAS = DeferredRegister.create(Registries.PARTICLE_TYPE, AelysiumMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<VoidBlossomEntity>> VOID_BLOSSOM = ENTIDADES.register("flor_do_vazio",
            () -> EntityType.Builder.<VoidBlossomEntity>of(VoidBlossomEntity::new, MobCategory.MONSTER)
                    .sized(8.0F, 10.0F)
                    .fireImmune()
                    .clientTrackingRange(3)
                    .build("flor_do_vazio"));

    public static final DeferredHolder<EntityType<?>, EntityType<SporeBallProjectile>> SPORE_BALL = ENTIDADES.register("bola_de_esporos",
            () -> EntityType.Builder.<SporeBallProjectile>of(SporeBallProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("bola_de_esporos"));

    public static final DeferredHolder<EntityType<?>, EntityType<PetalBladeProjectile>> PETAL_BLADE = ENTIDADES.register("lamina_de_petala",
            () -> EntityType.Builder.<PetalBladeProjectile>of(PetalBladeProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("lamina_de_petala"));

    public static final DeferredBlock<Block> VOID_BLOSSOM_BLOCK = BLOCOS.register("flor_do_vazio_curativa",
            () -> new VoidBlossomBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instabreak().noCollission().lightLevel(estado -> 11).sound(SoundType.SPORE_BLOSSOM)));

    public static final DeferredBlock<Block> MUSGO_VAZIO = BLOCOS.register("musgo_do_vazio",
            () -> new MusgoVazioBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_BLOCK)));

    public static final DeferredBlock<Block> MUSGO_FOFO = BLOCOS.register("musgo_fofo",
            () -> new MusgoFofoBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWDER_SNOW).mapColor(MapColor.COLOR_GREEN).sound(SoundType.MOSS)));

    public static final DeferredBlock<Block> VINE_WALL = BLOCOS.register("muro_de_vinhas",
            () -> new VineWallBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).sound(SoundType.WOOD).strength(2.0F, 6.0F)));

    public static final DeferredItem<BlockItem> VOID_BLOSSOM_BLOCK_ITEM = ITENS.registerSimpleBlockItem(VOID_BLOSSOM_BLOCK);
    public static final DeferredItem<BlockItem> VINE_WALL_ITEM = ITENS.registerSimpleBlockItem(VINE_WALL);

    public static final DeferredItem<Item> OVO_FLOR_DO_VAZIO = ITENS.register("flor_do_vazio_spawn_egg",
            () -> new DeferredSpawnEggItem(VOID_BLOSSOM, 0x1B0F2A, 0x7C3FB5, new Item.Properties()));

    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_BLOSSOM_HURT = som("flor_vazio.ferida");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_BLOSSOM_FALL = som("flor_vazio.queda");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_BLOSSOM_SPIKE = som("flor_vazio.espinho");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_BLOSSOM_BURROW = som("flor_vazio.enterrar");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPORE_PREPARE = som("flor_vazio.esporo_preparar");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPORE_IMPACT = som("flor_vazio.esporo_impacto");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPORE_BALL_LAND = som("flor_vazio.esporo_pouso");
    public static final DeferredHolder<SoundEvent, SoundEvent> PETAL_BLADE_SOM = som("flor_vazio.lamina_petala");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_SPIKE_INDICATOR = som("flor_vazio.espinho_aviso");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIKE_WAVE_INDICATOR = som("flor_vazio.onda_aviso");

    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, TipoParticulaSimples> DISAPPEARING_SWIRL = particula("flor_vazio_redemoinho", true);
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, TipoParticulaSimples> OBSIDILITH_BURST = particula("flor_vazio_explosao", true);
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, TipoParticulaSimples> SPARKLES = particula("flor_vazio_brilhos", true);
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, TipoParticulaSimples> LINE = particula("flor_vazio_linha", true);
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, TipoParticulaSimples> VOID_BLOSSOM_SPIKE_INDICATOR = particula("flor_vazio_aviso_espinho", true);
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, TipoParticulaSimples> VOID_BLOSSOM_SPIKE_WAVE_INDICATOR = particula("flor_vazio_aviso_onda", true);
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, TipoParticulaSimples> PETAL = particula("flor_vazio_petala", true);
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, TipoParticulaSimples> SPORE = particula("flor_vazio_esporo", true);
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, TipoParticulaSimples> SPORE_INDICATOR = particula("flor_vazio_aviso_esporo", true);
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>, TipoParticulaSimples> FLUFF = particula("flor_vazio_penugem", false);

    public static final List<DeferredItem<? extends Item>> ITENS_ABA = List.of(OVO_FLOR_DO_VAZIO, VOID_BLOSSOM_BLOCK_ITEM, VINE_WALL_ITEM);

    private RegistroFlorVazio() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> som(String nome) {
        return SONS.register(nome, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, nome)));
    }

    private static DeferredHolder<net.minecraft.core.particles.ParticleType<?>, TipoParticulaSimples> particula(String nome, boolean ignorarLimite) {
        return PARTICULAS.register(nome, () -> new TipoParticulaSimples(ignorarLimite));
    }

    public static void registrar(IEventBus barramento) {
        ENTIDADES.register(barramento);
        BLOCOS.register(barramento);
        ITENS.register(barramento);
        SONS.register(barramento);
        PARTICULAS.register(barramento);
        barramento.addListener(RegistroFlorVazio::criarAtributos);
    }

    private static void criarAtributos(EntityAttributeCreationEvent evento) {
        evento.put(VOID_BLOSSOM.get(), Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, VIDA)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.ATTACK_DAMAGE, ATAQUE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 10.0)
                .add(Attributes.ARMOR, ARMADURA)
                .build());
    }
}
