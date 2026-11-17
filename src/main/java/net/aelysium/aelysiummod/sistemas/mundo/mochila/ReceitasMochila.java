package net.aelysium.aelysiummod.sistemas.mundo.mochila;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ReceitasMochila {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZADORES =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, AelysiumMod.MOD_ID);

    public static final Supplier<RecipeSerializer<ReceitaTingirMochila>> TINGIR =
            SERIALIZADORES.register("mochila_tingir", () -> new SimpleCraftingRecipeSerializer<>(ReceitaTingirMochila::new));

    private ReceitasMochila() {
    }

    public static void registrar(IEventBus barramento) {
        SERIALIZADORES.register(barramento);
    }
}
