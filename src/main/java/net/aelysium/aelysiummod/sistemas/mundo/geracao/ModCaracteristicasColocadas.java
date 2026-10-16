package net.aelysium.aelysiummod.sistemas.mundo.geracao;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

public class ModCaracteristicasColocadas {
    public static final ResourceKey<PlacedFeature> END_CYANNAZ_MINERIO_PLACED_KEY = registerKey("end_cyannaz_minerio_placed");
    public static final ResourceKey<PlacedFeature> OVERWORLD_DN_FLOR_PLACED_KEY = registerKey("overworld_dn_flor_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, END_CYANNAZ_MINERIO_PLACED_KEY, configuredFeatures.getOrThrow(ModCaracteristicasConfiguradas.END_CIANNAZ_MINERIO_KEY),
                ModColocacaoMinerio.commonOrePlacement(12, HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(70))));

        register(context, OVERWORLD_DN_FLOR_PLACED_KEY, configuredFeatures.getOrThrow(ModCaracteristicasConfiguradas.OVERWORLD_DN_FLOR_KEY),
                VegetationPlacements.worldSurfaceSquaredWithCount(1));

    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
