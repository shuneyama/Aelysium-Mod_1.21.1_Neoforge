package net.aelysium.aelysiummod.datagen;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModBlocos;
import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProvedorReceitas extends RecipeProvider implements IConditionBuilder {
    public ProvedorReceitas(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        List<ItemLike> CIANE_SMELTABLES = List.of(ModItens.CIANE_CRU);


        oreSmelting(recipeOutput, CIANE_SMELTABLES, RecipeCategory.MISC, ModItens.CIANE_FRAGMENTO.get(), 0.3f, 200, "ciane");
        oreBlasting(recipeOutput, CIANE_SMELTABLES, RecipeCategory.MISC, ModItens.CIANE_FRAGMENTO.get(), 0.3f, 100, "ciane");


ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItens.CIANE_CAPACETE.get())
                .pattern("CCC")
                .pattern("C C")
                .define('C', ModItens.CIANE_BARRA.get())
                .unlockedBy("has_ciane_barra", has(ModItens.CIANE_BARRA)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItens.CIANE_PEITORAL.get())
                .pattern("C C")
                .pattern("CCC")
                .pattern("CCC")
                .define('C', ModItens.CIANE_BARRA.get())
                .unlockedBy("has_ciane_barra", has(ModItens.CIANE_BARRA)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItens.CIANE_CALCINHA.get())
                .pattern("CCC")
                .pattern("C C")
                .pattern("C C")
                .define('C', ModItens.CIANE_BARRA.get())
                .unlockedBy("has_ciane_barra", has(ModItens.CIANE_BARRA)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItens.CIANE_BOTAS.get())
                .pattern("C C")
                .pattern("C C")
                .define('C', ModItens.CIANE_BARRA.get())
                .unlockedBy("has_ciane_barra", has(ModItens.CIANE_BARRA)).save(recipeOutput);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItens.CIANE_PICARETA.get())
                .pattern("CCC")
                .pattern(" S ")
                .pattern(" S ")
                .define('C', ModItens.CIANE_BARRA.get())
                .define('S', Items.STICK)
                .unlockedBy("has_ciane_barra", has(ModItens.CIANE_BARRA)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItens.CIANE_MACHADO.get())
                .pattern(" CC")
                .pattern(" SC")
                .pattern(" S ")
                .define('C', ModItens.CIANE_BARRA.get())
                .define('S', Items.STICK)
                .unlockedBy("has_ciane_barra", has(ModItens.CIANE_BARRA)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItens.CIANE_ENXADA.get())
                .pattern(" CC")
                .pattern(" S ")
                .pattern(" S ")
                .define('C', ModItens.CIANE_BARRA.get())
                .define('S', Items.STICK)
                .unlockedBy("has_ciane_barra", has(ModItens.CIANE_BARRA)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItens.CIANE_PA.get())
                .pattern(" C ")
                .pattern(" S ")
                .pattern(" S ")
                .define('C', ModItens.CIANE_BARRA.get())
                .define('S', Items.STICK)
                .unlockedBy("has_ciane_barra", has(ModItens.CIANE_BARRA)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItens.CIANE_ESPADA.get())
                .pattern("NCN")
                .pattern(" N ")
                .pattern(" C ")
                .define('C', ModItens.CIANE_BARRA.get())
                .define('N', Items.NETHER_STAR)
                .unlockedBy("has_ciane_barra", has(ModItens.CIANE_BARRA)).save(recipeOutput);


ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItens.CIANE_BARRA.get(), 1)
                .requires(ModItens.CIANE_FRAGMENTO)
                .requires(ModItens.CIANE_FRAGMENTO)
                .requires(ModItens.CIANE_FRAGMENTO)
                .requires(ModItens.CIANE_FRAGMENTO)
                .requires(Items.NETHERITE_INGOT)
                .requires(Items.NETHERITE_INGOT)
                .requires(Items.NETHERITE_INGOT)
                .requires(Items.NETHERITE_INGOT)
                .unlockedBy("has_ciane_scrap", has(ModItens.CIANE_FRAGMENTO))
                .save(recipeOutput, "aelysiummod:ciane_ingot");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItens.POEIRA_LUNAR.get(), 1)
                .requires(ModBlocos.DAMA_DA_NOITE)
                .unlockedBy("has_dama_da_noite", has(ModBlocos.DAMA_DA_NOITE))
                .save(recipeOutput);

    }

    protected static void oreSmelting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTime, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput recipeOutput, RecipeSerializer<T> pCookingSerializer, AbstractCookingRecipe.Factory<T> factory,
                                                                       List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer, factory).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(recipeOutput, AelysiumMod.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }
}
