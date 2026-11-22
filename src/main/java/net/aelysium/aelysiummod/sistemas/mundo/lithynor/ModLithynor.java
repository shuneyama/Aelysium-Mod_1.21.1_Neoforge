package net.aelysium.aelysiummod.sistemas.mundo.lithynor;

import com.mojang.serialization.MapCodec;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModLithynor {

    private static final DeferredRegister<MapCodec<? extends BiomeSource>> FONTES_BIOMA =
            DeferredRegister.create(Registries.BIOME_SOURCE, AelysiumMod.MOD_ID);

    public static final Supplier<MapCodec<FonteBiomasLithynor>> FONTE_LITHYNOR =
            FONTES_BIOMA.register("lithynor", () -> FonteBiomasLithynor.CODEC);

    private static final DeferredRegister<MapCodec<? extends DensityFunction>> FUNCOES_DENSIDADE =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, AelysiumMod.MOD_ID);

    public static final Supplier<MapCodec<DistanciaCentral>> DISTANCIA_CENTRAL =
            FUNCOES_DENSIDADE.register("distancia_central", () -> DistanciaCentral.MAPA);

    public static final Supplier<MapCodec<GradienteZ>> GRADIENTE_Z =
            FUNCOES_DENSIDADE.register("gradiente_z", () -> GradienteZ.MAPA);

    private ModLithynor() {
    }

    public static void registrar(IEventBus barramento) {
        FONTES_BIOMA.register(barramento);
        FUNCOES_DENSIDADE.register(barramento);
    }
}
