package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import com.mojang.logging.LogUtils;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoListaBanimentos;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoBanimentoReceitas;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class GerenciadorFiltroReceitas {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static Object cachedOriginalRecipeMap = null;
    private static RecipeManager cachedRecipeManager = null;
    private static List<RecipeHolder<?>> lastFilteredList = List.of();
    private static MinecraftServer currentServer = null;

    public static void setServer(MinecraftServer server) {
        currentServer = server;
    }

    public static void captureAndApplyFilter(RecipeManager rm, HolderLookup.Provider registries) {
        try {
            Field recipesField = getRecipesField();
            cachedOriginalRecipeMap = recipesField.get(rm);
            cachedRecipeManager = rm;
            applyFilterInternal(rm, cachedOriginalRecipeMap, registries, recipesField);
            if (currentServer != null) {
                syncToAllPlayers(currentServer);
            }
        } catch (Exception e) {
            LOGGER.error("[AelysiumMod] Falha ao filtrar receitas: {}", e.getMessage());
        }
    }

    public static void reapplyFilter(MinecraftServer server) {
        if (cachedOriginalRecipeMap == null || cachedRecipeManager == null) {
            LOGGER.warn("[AelysiumMod] Filtro de receitas ainda não inicializado. Aguarde o servidor carregar completamente.");
            return;
        }
        try {
            Field recipesField = getRecipesField();
            applyFilterInternal(cachedRecipeManager, cachedOriginalRecipeMap, server.registryAccess(), recipesField);
            syncToAllPlayers(server);
        } catch (Exception e) {
            LOGGER.error("[AelysiumMod] Falha ao reaplicar filtro de receitas: {}", e.getMessage());
        }
    }

    private static void applyFilterInternal(RecipeManager rm, Object originalRecipeMap,
                                             HolderLookup.Provider registries, Field recipesField) throws Exception {
        Class<?> recipeMapClass = getRecipeMapClass();

        Field byTypeField = recipeMapClass.getDeclaredField("byType");
        byTypeField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> byType =
            (Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>>) byTypeField.get(originalRecipeMap);

        Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> filteredByType = new LinkedHashMap<>();
        List<RecipeHolder<?>> filteredList = new ArrayList<>();

        for (Map.Entry<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> typeEntry : byType.entrySet()) {
            Map<ResourceLocation, RecipeHolder<?>> filteredForType = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, RecipeHolder<?>> recipeEntry : typeEntry.getValue().entrySet()) {
                if (!shouldRemove(recipeEntry.getValue(), registries)) {
                    filteredForType.put(recipeEntry.getKey(), recipeEntry.getValue());
                    filteredList.add(recipeEntry.getValue());
                }
            }
            if (!filteredForType.isEmpty()) {
                filteredByType.put(typeEntry.getKey(), Collections.unmodifiableMap(filteredForType));
            }
        }


        Constructor<?> ctor = Arrays.stream(recipeMapClass.getDeclaredConstructors())
            .filter(c -> c.getParameterCount() == 2)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("RecipeMap constructor not found"));
        ctor.setAccessible(true);
        Object newRecipeMap = ctor.newInstance(
            Collections.unmodifiableMap(filteredByType),
            Collections.unmodifiableList(filteredList)
        );

        recipesField.set(rm, newRecipeMap);
        lastFilteredList = filteredList;
    }

    private static boolean shouldRemove(RecipeHolder<?> holder, HolderLookup.Provider registries) {
        try {
            if (ConfiguracaoModsDesativados.isReceitasCapped(holder.id().getNamespace())) return true;
            Recipe<?> recipe = holder.value();
            ItemStack result = recipe.getResultItem(registries);
            if (result.isEmpty()) return false;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(result.getItem());

            if (ConfiguracaoListaBanimentos.isBanned(id) || ConfiguracaoBanimentoReceitas.isBlocked(id)) return true;
            if (ConfiguracaoModsDesativados.isReceitasCapped(id.getNamespace())) return true;

            for (net.minecraft.world.item.crafting.Ingredient ingredient : recipe.getIngredients()) {
                for (ItemStack stack : ingredient.getItems()) {
                    ResourceLocation ingId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (ConfiguracaoListaBanimentos.isBanned(ingId)) return true;
                }
            }

            for (net.minecraft.world.item.crafting.Ingredient ingredient : getSmithingIngredients(recipe)) {
                for (ItemStack stack : ingredient.getItems()) {
                    ResourceLocation ingId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (ConfiguracaoListaBanimentos.isBanned(ingId)) return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static List<net.minecraft.world.item.crafting.Ingredient> getSmithingIngredients(Recipe<?> recipe) {
        if (!(recipe instanceof net.minecraft.world.item.crafting.SmithingRecipe)) return List.of();

        List<net.minecraft.world.item.crafting.Ingredient> ingredientes = new ArrayList<>();
        for (String campo : new String[]{"template", "base", "addition"}) {
            try {
                Field f = recipe.getClass().getDeclaredField(campo);
                f.setAccessible(true);
                Object valor = f.get(recipe);
                if (valor instanceof net.minecraft.world.item.crafting.Ingredient ingrediente) {
                    ingredientes.add(ingrediente);
                }
            } catch (Exception ignored) {}
        }
        return ingredientes;
    }

    public static void syncToAllPlayers(MinecraftServer server) {
        if (lastFilteredList.isEmpty() && server.getRecipeManager().getOrderedRecipes().isEmpty()) return;
        ClientboundUpdateRecipesPacket packet = new ClientboundUpdateRecipesPacket(lastFilteredList);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(packet);
        }
    }

    public static void syncToPlayer(ServerPlayer player) {
        if (lastFilteredList.isEmpty()) return;
        player.connection.send(new ClientboundUpdateRecipesPacket(lastFilteredList));
    }

    private static Field getRecipesField() throws NoSuchFieldException {
        Field f = RecipeManager.class.getDeclaredField("recipes");
        f.setAccessible(true);
        return f;
    }

    private static Class<?> getRecipeMapClass() {
        return Arrays.stream(RecipeManager.class.getDeclaredClasses())
            .filter(c -> c.getSimpleName().equals("RecipeMap"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("RecipeMap inner class not found in RecipeManager"));
    }

    public static boolean isRecipeResultBlocked(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return ConfiguracaoBanimentoReceitas.isBlocked(id);
    }
}
