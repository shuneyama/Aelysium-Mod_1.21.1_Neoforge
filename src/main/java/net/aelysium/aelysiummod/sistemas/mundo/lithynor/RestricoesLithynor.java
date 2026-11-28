package net.aelysium.aelysiummod.sistemas.mundo.lithynor;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.fml.ModList;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class RestricoesLithynor {

    public static final ResourceLocation DIMENSAO = ResourceLocation.fromNamespaceAndPath("aelysium", "lithynor");
    public static final int LIMITE_SUL = 3000;
    public static final int LIMITE_NORTE = -3000;

    private static final Set<String> MODS_EXCLUIDOS = Set.of(
            "dungeons_arise",
            "dungeons_arise_seven_seas",
            "terrablender",
            "terralith",
            "ae2",
            "biolith",
            "create",
            "croptopia",
            "deeperdarker",
            "regions_unexplored",
            "tectonic",
            "lithostitched",
            "apollib"
    );

    private static final Map<ResourceLocation, String> ESTRUTURAS_ALTERADAS_POR_MOD = Map.of(
            ResourceLocation.withDefaultNamespace("ancient_city"), "deeperdarker"
    );

    private static final ResourceLocation IGLU = ResourceLocation.withDefaultNamespace("igloo");

    private static final Set<ResourceLocation> ESTRUTURAS_NEVE = Set.of(
            ResourceLocation.withDefaultNamespace("village_snowy")
    );

    private RestricoesLithynor() {
    }

    public static boolean modExcluido(String namespace) {
        return MODS_EXCLUIDOS.contains(namespace);
    }

    public static boolean ehLithynor(ChunkGenerator gerador) {
        return gerador.getBiomeSource() instanceof FonteBiomasLithynor;
    }

    public static boolean ehLithynor(LevelAccessor nivel) {
        return nivel instanceof Level mundo && mundo.dimension().location().equals(DIMENSAO);
    }

    public static boolean estruturaExcluida(ResourceLocation estrutura) {
        if (modExcluido(estrutura.getNamespace())) return true;
        String mod = ESTRUTURAS_ALTERADAS_POR_MOD.get(estrutura);
        return mod != null && ModList.get().isLoaded(mod);
    }

    public static boolean estruturaPermitida(ResourceLocation estrutura, ChunkPos posicao) {
        if (estruturaExcluida(estrutura)) return false;
        return !(estrutura.equals(IGLU) && posicao.getMiddleBlockZ() <= LIMITE_NORTE);
    }

    public static Predicate<Holder<Biome>> biomasValidos(Predicate<Holder<Biome>> original, ResourceLocation estrutura, ChunkPos posicao, FonteBiomasLithynor fonte) {
        if (!usaEquivalentes(estrutura, posicao.getMiddleBlockZ())) return original;
        return bioma -> original.test(bioma) || original.test(fonte.equivalenteEstrutura(bioma));
    }

    private static boolean usaEquivalentes(ResourceLocation estrutura, int z) {
        String namespace = estrutura.getNamespace();
        if (namespace.equals(AelysiumMod.MOD_ID) || modExcluido(namespace)) return false;
        if (z >= LIMITE_SUL) return true;
        if (z <= LIMITE_NORTE) return !namespace.equals(ResourceLocation.DEFAULT_NAMESPACE) || ESTRUTURAS_NEVE.contains(estrutura);
        return false;
    }
}
