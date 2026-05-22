package net.aelysium.aelysiummod.npc;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
import net.aelysium.aelysiummod.item.custom.CriadorNPC;
import net.aelysium.aelysiummod.item.custom.CriadorRegistro;
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

public class NpcRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, AelysiumMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<CustomNpcEntity>> CUSTOM_NPC =
            ENTITY_TYPES.register("custom_npc", () ->
                    EntityType.Builder.<CustomNpcEntity>of(CustomNpcEntity::new, MobCategory.MISC)
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
