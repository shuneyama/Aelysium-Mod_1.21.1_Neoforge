package net.aelysium.aelysiummod.datagen;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.mundo.geracao.ModModificadoresBioma;
import net.aelysium.aelysiummod.sistemas.mundo.geracao.ModCaracteristicasConfiguradas;
import net.aelysium.aelysiummod.sistemas.mundo.geracao.ModCaracteristicasColocadas;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ProvedorDatapack extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModCaracteristicasConfiguradas::bootstrap)
            .add(Registries.PLACED_FEATURE, ModCaracteristicasColocadas::bootstrap)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModModificadoresBioma::bootstrap);

    public ProvedorDatapack(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(AelysiumMod.MOD_ID));
    }
}