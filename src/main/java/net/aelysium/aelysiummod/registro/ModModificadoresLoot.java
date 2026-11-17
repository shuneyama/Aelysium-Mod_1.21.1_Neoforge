package net.aelysium.aelysiummod.registro;

import com.mojang.serialization.MapCodec;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ModificadorLootModsDesativados;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ModificadorLootItemBanido;

public class ModModificadoresLoot {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, AelysiumMod.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ModificadorLootItemBanido>> BANNED_ITEM_REDUCER =
            LOOT_MODIFIER_SERIALIZERS.register("banned_item_reducer", () -> ModificadorLootItemBanido.CODEC);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ModificadorLootModsDesativados>> MOD_CAP_LOOT_REDUCER =
            LOOT_MODIFIER_SERIALIZERS.register("mod_cap_loot_reducer", () -> ModificadorLootModsDesativados.CODEC);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<net.aelysium.aelysiummod.sistemas.admin.banimentos.ModificadorLootSela>> SADDLE_TO_LEATHER =
            LOOT_MODIFIER_SERIALIZERS.register("saddle_to_leather", () -> net.aelysium.aelysiummod.sistemas.admin.banimentos.ModificadorLootSela.CODEC);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<net.aelysium.aelysiummod.sistemas.admin.banimentos.ModificadorLootCogumelo>> ZOMBIE_HORSE_MUSHROOM =
            LOOT_MODIFIER_SERIALIZERS.register("zombie_horse_mushroom", () -> net.aelysium.aelysiummod.sistemas.admin.banimentos.ModificadorLootCogumelo.CODEC);

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }
}
