package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class OuvinteFiltroReceitas implements PreparableReloadListener {

    private final RecipeManager recipeManager;
    private final HolderLookup.Provider registries;

    public OuvinteFiltroReceitas(RecipeManager recipeManager, HolderLookup.Provider registries) {
        this.recipeManager = recipeManager;
        this.registries = registries;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager,
                                          ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler,
                                          Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() -> null, backgroundExecutor)
            .thenCompose(barrier::wait)
            .thenRunAsync(() -> GerenciadorFiltroReceitas.captureAndApplyFilter(recipeManager, registries), gameExecutor);
    }
}
