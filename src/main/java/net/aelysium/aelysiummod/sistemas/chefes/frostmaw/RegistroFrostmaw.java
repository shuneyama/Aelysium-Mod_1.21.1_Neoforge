package net.aelysium.aelysiummod.sistemas.chefes.frostmaw;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.particula.DadosFlocoNeve;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.particula.DadosAnel;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.particula.DadosNuvem;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.dados.FrozenData;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.efeitos.EntityBlockSwapper;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.efeitos.EntityCameraShake;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.efeitos.EntityIceBall;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.efeitos.EntityIceBreath;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.EntityFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.EntityFrozenController;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.efeito.EffectFrozen;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public final class RegistroFrostmaw {

    public static final boolean TEM_BARRA_CHEFE = true;
    public static final boolean RESETA_VIDA_AO_RENASCER = true;
    public static final boolean TREMER_CAMERA = true;

    private static final DeferredRegister<EntityType<?>> ENTIDADES = DeferredRegister.create(Registries.ENTITY_TYPE, AelysiumMod.MOD_ID);
    private static final DeferredRegister.Items ITENS = DeferredRegister.createItems(AelysiumMod.MOD_ID);
    private static final DeferredRegister<SoundEvent> SONS = DeferredRegister.create(Registries.SOUND_EVENT, AelysiumMod.MOD_ID);
    private static final DeferredRegister<ParticleType<?>> PARTICULAS = DeferredRegister.create(Registries.PARTICLE_TYPE, AelysiumMod.MOD_ID);
    private static final DeferredRegister<MobEffect> EFEITOS = DeferredRegister.create(Registries.MOB_EFFECT, AelysiumMod.MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ANEXOS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AelysiumMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFrostmaw>> FROSTMAW = ENTIDADES.register("frostmaw",
            () -> EntityType.Builder.<EntityFrostmaw>of(EntityFrostmaw::new, MobCategory.MONSTER).sized(4F, 4F).clientTrackingRange(10).updateInterval(1).build("frostmaw"));
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIceBreath>> ICE_BREATH = ENTIDADES.register("sopro_de_gelo",
            () -> EntityType.Builder.<EntityIceBreath>of(EntityIceBreath::new, MobCategory.MISC).sized(0F, 0F).updateInterval(1).build("sopro_de_gelo"));
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIceBall>> ICE_BALL = ENTIDADES.register("bola_de_gelo",
            () -> EntityType.Builder.<EntityIceBall>of(EntityIceBall::new, MobCategory.MISC).sized(0.5F, 0.5F).updateInterval(20).build("bola_de_gelo"));
    public static final DeferredHolder<EntityType<?>, EntityType<EntityFrozenController>> FROZEN_CONTROLLER = ENTIDADES.register("controlador_congelamento",
            () -> EntityType.Builder.<EntityFrozenController>of(EntityFrozenController::new, MobCategory.MISC).noSummon().sized(0F, 0F).build("controlador_congelamento"));
    public static final DeferredHolder<EntityType<?>, EntityType<EntityBlockSwapper>> BLOCK_SWAPPER = ENTIDADES.register("trocador_de_bloco",
            () -> EntityType.Builder.<EntityBlockSwapper>of(EntityBlockSwapper::new, MobCategory.MISC).noSummon().sized(1F, 1F).updateInterval(Integer.MAX_VALUE).build("trocador_de_bloco"));
    public static final DeferredHolder<EntityType<?>, EntityType<EntityCameraShake>> CAMERA_SHAKE = ENTIDADES.register("tremor_de_camera",
            () -> EntityType.Builder.<EntityCameraShake>of(EntityCameraShake::new, MobCategory.MISC).sized(1F, 1F).updateInterval(Integer.MAX_VALUE).build("tremor_de_camera"));


    public static final DeferredItem<Item> OVO_FROSTMAW = ITENS.register("frostmaw_spawn_egg",
            () -> new DeferredSpawnEggItem(FROSTMAW, 0xF7FAFF, 0x0B6FB9, new Item.Properties()));

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROSTMAW_ROAR = som("frostmaw.roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROSTMAW_DIE = som("frostmaw.die");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROSTMAW_WHOOSH = som("frostmaw.whoosh");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROSTMAW_ICEBREATH = som("frostmaw.icebreath");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROSTMAW_ICEBREATH_START = som("frostmaw.icebreathstart");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROSTMAW_ICEBALL_CHARGE = som("frostmaw.iceballcharge");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROSTMAW_ICEBALL_SHOOT = som("frostmaw.iceballshoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROSTMAW_FROZEN_CRASH = som("frostmaw.frozencrash");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROSTMAW_STEP = som("frostmaw.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROSTMAW_LAND = som("frostmaw.land");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROSTMAW_WAKEUP = som("frostmaw.wakeup");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROSTMAW_UNDAMAGED = som("frostmaw.undamaged");
    public static final ImmutableList<DeferredHolder<SoundEvent, SoundEvent>> ENTITY_FROSTMAW_ATTACK = ImmutableList.of(
            som("frostmaw.attack1"), som("frostmaw.attack2"), som("frostmaw.attack3"), som("frostmaw.attack4"));
    public static final ImmutableList<DeferredHolder<SoundEvent, SoundEvent>> ENTITY_FROSTMAW_BREATH = ImmutableList.of(
            som("frostmaw.breath1"), som("frostmaw.breath2"));
    public static final ImmutableList<DeferredHolder<SoundEvent, SoundEvent>> ENTITY_FROSTMAW_LIVING = ImmutableList.of(
            som("frostmaw.living1"), som("frostmaw.living2"));

    public static final DeferredHolder<ParticleType<?>, ParticleType<DadosFlocoNeve>> SNOWFLAKE = PARTICULAS.register("frostmaw_floco_de_neve",
            () -> tipoParticula(DadosFlocoNeve.CODEC, DadosFlocoNeve.STREAM_CODEC));
    public static final DeferredHolder<ParticleType<?>, ParticleType<DadosNuvem>> CLOUD = PARTICULAS.register("frostmaw_nuvem",
            () -> tipoParticula(DadosNuvem.CODEC, DadosNuvem.STREAM_CODEC));
    public static final DeferredHolder<ParticleType<?>, ParticleType<DadosAnel>> RING = PARTICULAS.register("frostmaw_anel",
            () -> tipoParticula(DadosAnel.CODEC, DadosAnel.STREAM_CODEC));

    public static final DeferredHolder<MobEffect, EffectFrozen> FROZEN = EFEITOS.register("congelado", EffectFrozen::new);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<FrozenData>> FROZEN_DATA = ANEXOS.register("dados_congelamento",
            () -> AttachmentType.serializable(FrozenData::new).build());

    public static final List<DeferredItem<? extends Item>> ITENS_ABA = List.of(OVO_FROSTMAW);

    private RegistroFrostmaw() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> som(String nome) {
        return SONS.register(nome, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, nome)));
    }

    private static <T extends net.minecraft.core.particles.ParticleOptions> ParticleType<T> tipoParticula(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> codecRede) {
        return new ParticleType<>(false) {
            @Override
            public MapCodec<T> codec() {
                return codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return codecRede;
            }
        };
    }

    public static void registrar(IEventBus barramento) {
        ENTIDADES.register(barramento);
        ITENS.register(barramento);
        SONS.register(barramento);
        PARTICULAS.register(barramento);
        EFEITOS.register(barramento);
        ANEXOS.register(barramento);
        barramento.addListener(RegistroFrostmaw::criarAtributos);
    }

    private static void criarAtributos(EntityAttributeCreationEvent evento) {
        evento.put(FROSTMAW.get(), EntityFrostmaw.createAttributes().build());
    }
}
