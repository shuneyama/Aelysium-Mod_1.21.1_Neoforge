package net.aelysium.aelysiummod.registro;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.aelysium.aelysiummod.conteudo.entidade.EntidadeBalaGlock;
import net.aelysium.aelysiummod.conteudo.entidade.EntidadeBarreiraPainel;
import net.aelysium.aelysiummod.conteudo.entidade.EntidadeTridenteConchas;

public class ModEntidades {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, AelysiumMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<EntidadeBalaGlock>> GLOCK_BALA =
            ENTITY_TYPES.register("glock_bala", () ->
                    EntityType.Builder.<EntidadeBalaGlock>of(EntidadeBalaGlock::new, MobCategory.MISC)
                            .sized(0.15F, 0.15F)
                            .clientTrackingRange(64)
                            .updateInterval(10)
                            .build("glock_bala")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<EntidadeTridenteConchas>> TRIDENTE_CONCHAS =
            ENTITY_TYPES.register("tridente_conchas", () ->
                    EntityType.Builder.<EntidadeTridenteConchas>of(EntidadeTridenteConchas::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .eyeHeight(0.13F)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("tridente_conchas")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<EntidadeBarreiraPainel>> BARREIRA_PAINEL =
            ENTITY_TYPES.register("barreira_painel", () ->
                    EntityType.Builder.<EntidadeBarreiraPainel>of(EntidadeBarreiraPainel::new, MobCategory.MISC)
                            .sized(0.3F, 0.3F)
                            .fireImmune()
                            .noSummon()
                            .clientTrackingRange(10)
                            .updateInterval(Integer.MAX_VALUE)
                            .build("barreira_painel")
            );

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
