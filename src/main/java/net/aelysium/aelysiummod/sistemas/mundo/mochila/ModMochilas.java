package net.aelysium.aelysiummod.sistemas.mundo.mochila;

import com.mojang.serialization.Codec;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BundleContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ModMochilas {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AelysiumMod.MOD_ID);

    public static final Supplier<DataComponentType<Integer>> SELECIONADO = COMPONENTES.register("mochila_selecionada",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    public static final DeferredRegister.Items ITENS = DeferredRegister.createItems(AelysiumMod.MOD_ID);

    public static final DeferredItem<Item> MOCHILA = ITENS.register("mochila", () -> criar());

    public static final Map<DyeColor, DeferredItem<Item>> COLORIDAS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor cor : DyeColor.values()) {
            COLORIDAS.put(cor, ITENS.register("mochila_" + cor.getName(), () -> criar()));
        }
    }

    private ModMochilas() {
    }

    private static Item criar() {
        return new ItemMochila(new Item.Properties().stacksTo(1).component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY));
    }

    public static boolean ehMochila(net.minecraft.world.item.ItemStack pilha) {
        return pilha.getItem() instanceof ItemMochila;
    }

    public static void registrar(IEventBus barramento) {
        COMPONENTES.register(barramento);
        ITENS.register(barramento);
    }
}
