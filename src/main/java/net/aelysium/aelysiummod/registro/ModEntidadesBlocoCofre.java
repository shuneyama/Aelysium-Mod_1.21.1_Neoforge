package net.aelysium.aelysiummod.registro;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModBlocos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.aelysium.aelysiummod.sistemas.mundo.porta.cofre.EntidadeBlocoCofreComPainel;

public class ModEntidadesBlocoCofre {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AelysiumMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EntidadeBlocoCofreComPainel>> COFRE_COM_PAINEL =
            BLOCK_ENTITIES.register("cofre_com_painel", () -> BlockEntityType.Builder.of(
                    EntidadeBlocoCofreComPainel::new, ModBlocos.COFRE_COM_PAINEL.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
