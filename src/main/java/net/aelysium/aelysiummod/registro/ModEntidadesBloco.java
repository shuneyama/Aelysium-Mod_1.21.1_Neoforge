package net.aelysium.aelysiummod.registro;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModBlocos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.aelysium.aelysiummod.sistemas.mundo.porta.EntidadeBlocoPortaSenha;

public class ModEntidadesBloco {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AelysiumMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EntidadeBlocoPortaSenha>> PORTA_SENHA =
            BLOCK_ENTITIES.register("porta_senha", () -> BlockEntityType.Builder.of(
                    EntidadeBlocoPortaSenha::new, ModBlocos.PORTA_UM_PAINEL.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
