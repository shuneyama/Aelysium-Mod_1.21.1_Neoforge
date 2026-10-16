package net.aelysium.aelysiummod.registro;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.aelysium.aelysiummod.sistemas.social.npc.item.CriadorNPC;
import net.aelysium.aelysiummod.sistemas.social.npc.item.CriadorRegistro;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RegistroNpc {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, AelysiumMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<EntidadeNpcPersonalizado>> CUSTOM_NPC =
            ENTITY_TYPES.register("custom_npc", () ->
                    EntityType.Builder.<EntidadeNpcPersonalizado>of(EntidadeNpcPersonalizado::new, MobCategory.MISC)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .fireImmune()
                            .build("custom_npc")
            );

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
