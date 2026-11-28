package net.aelysium.aelysiummod.sistemas.mundo.lithynor;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.aelysium.aelysiummod.sistemas.mundo.lithynor.mixin.AcessorFonteMultiRuido;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FonteBiomasLithynor extends BiomeSource {

    public static final float LIMIAR_PLANO = 0.78F;
    public static final float LIMIAR_PICOS = 0.86F;
    public static final float LIMIAR_COGUMELO = 0.95F;
    public static final float MARCA_TERRITORIO = 3.0F;
    private static final float LIMIAR_TERRITORIO = 2.0F;

    public record Troca(Holder<Biome> de, Holder<Biome> para) {
        public static final Codec<Troca> CODEC = RecordCodecBuilder.create(i -> i.group(
                Biome.CODEC.fieldOf("from").forGetter(Troca::de),
                Biome.CODEC.fieldOf("to").forGetter(Troca::para)
        ).apply(i, Troca::new));
    }

    public static final MapCodec<FonteBiomasLithynor> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiomeSource.CODEC.fieldOf("source").forGetter(s -> s.base),
            Troca.CODEC.listOf().fieldOf("replacements").forGetter(s -> s.trocas),
            Biome.CODEC.fieldOf("flat_rocky").forGetter(s -> s.planoPedregoso),
            Biome.CODEC.fieldOf("flat_farm").forGetter(s -> s.planoCultivado),
            Biome.CODEC.fieldOf("flat_flowery").forGetter(s -> s.planoFlorido),
            Biome.CODEC.fieldOf("mushroom").forGetter(s -> s.cogumelo),
            Biome.CODEC.fieldOf("rock_peaks").forGetter(s -> s.picos),
            Troca.CODEC.listOf().fieldOf("far_replacements").forGetter(s -> s.trocasLonge),
            Codec.INT.fieldOf("far_radius").forGetter(s -> s.raioLonge),
            Troca.CODEC.listOf().fieldOf("cold_replacements").forGetter(s -> s.trocasFrio),
            Codec.FLOAT.fieldOf("cold_temperature").forGetter(s -> s.temperaturaFrio),
            Troca.CODEC.listOf().optionalFieldOf("territory_replacements", List.of()).forGetter(s -> s.trocasTerritorio),
            Troca.CODEC.listOf().optionalFieldOf("isolated_replacements", List.of()).forGetter(s -> s.trocasIsoladas),
            Troca.CODEC.listOf().optionalFieldOf("structure_equivalents", List.of()).forGetter(s -> s.equivalentesEstrutura)
    ).apply(i, FonteBiomasLithynor::new));

    private final BiomeSource base;
    private final List<Troca> trocas;
    private final Holder<Biome> planoPedregoso;
    private final Holder<Biome> planoCultivado;
    private final Holder<Biome> planoFlorido;
    private final Holder<Biome> cogumelo;
    private final Holder<Biome> picos;
    private final List<Troca> trocasLonge;
    private final int raioLonge;
    private final List<Troca> trocasFrio;
    private final float temperaturaFrio;
    private final List<Troca> trocasTerritorio;
    private final List<Troca> trocasIsoladas;
    private final List<Troca> equivalentesEstrutura;
    private final Map<ResourceKey<Biome>, Holder<Biome>> mapaTerritorio = new HashMap<>();
    private final Map<ResourceKey<Biome>, Holder<Biome>> mapaFrio = new HashMap<>();
    private final Map<ResourceKey<Biome>, Holder<Biome>> mapaLonge = new HashMap<>();
    private final Map<ResourceKey<Biome>, Holder<Biome>> mapa = new HashMap<>();
    private final Map<ResourceKey<Biome>, Holder<Biome>> mapaIsolado = new HashMap<>();
    private final Map<ResourceKey<Biome>, Holder<Biome>> mapaEquivalente = new HashMap<>();
    private volatile Set<Holder<Biome>> biomasEstruturas;

    public FonteBiomasLithynor(BiomeSource base, List<Troca> trocas, Holder<Biome> planoPedregoso, Holder<Biome> planoCultivado, Holder<Biome> planoFlorido, Holder<Biome> cogumelo, Holder<Biome> picos, List<Troca> trocasLonge, int raioLonge, List<Troca> trocasFrio, float temperaturaFrio, List<Troca> trocasTerritorio, List<Troca> trocasIsoladas, List<Troca> equivalentesEstrutura) {
        this.base = base;
        this.trocas = trocas;
        this.planoPedregoso = planoPedregoso;
        this.planoCultivado = planoCultivado;
        this.planoFlorido = planoFlorido;
        this.cogumelo = cogumelo;
        this.picos = picos;
        this.trocasLonge = trocasLonge;
        this.raioLonge = raioLonge;
        this.trocasFrio = trocasFrio;
        this.temperaturaFrio = temperaturaFrio;
        this.trocasTerritorio = trocasTerritorio;
        this.trocasIsoladas = trocasIsoladas;
        this.equivalentesEstrutura = equivalentesEstrutura;
        registrar(trocasTerritorio, mapaTerritorio);
        registrar(trocasFrio, mapaFrio);
        registrar(trocasLonge, mapaLonge);
        registrar(trocas, mapa);
        registrar(trocasIsoladas, mapaIsolado);
        registrar(equivalentesEstrutura, mapaEquivalente);
    }

    private static void registrar(List<Troca> lista, Map<ResourceKey<Biome>, Holder<Biome>> destino) {
        for (Troca troca : lista) {
            troca.de().unwrapKey().ifPresent(chave -> destino.put(chave, troca.para()));
        }
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(
                biomasBase().map(this::substituir),
                trocasLonge.stream().map(Troca::para),
                trocasFrio.stream().map(Troca::para),
                trocasTerritorio.stream().map(Troca::para),
                Stream.of(planoPedregoso, planoCultivado, planoFlorido, cogumelo, picos)
        ).flatMap(fluxo -> fluxo).map(this::isolar).distinct();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler amostrador) {
        return isolar(escolher(x, y, z, amostrador));
    }

    public Holder<Biome> equivalenteEstrutura(Holder<Biome> bioma) {
        return bioma.unwrapKey().map(mapaEquivalente::get).orElse(bioma);
    }

    public Set<Holder<Biome>> biomasParaEstruturas() {
        Set<Holder<Biome>> atual = biomasEstruturas;
        if (atual == null) {
            atual = Stream.concat(possibleBiomes().stream(), equivalentesEstrutura.stream().map(Troca::para))
                    .collect(Collectors.toUnmodifiableSet());
            biomasEstruturas = atual;
        }
        return atual;
    }

    private Holder<Biome> escolher(int x, int y, int z, Climate.Sampler amostrador) {
        Climate.TargetPoint bruto = amostrador.sample(x, y, z);
        boolean territorio = Climate.unquantizeCoord(bruto.continentalness()) >= LIMIAR_TERRITORIO;
        Climate.TargetPoint ponto = territorio ? semMarcaTerritorio(bruto) : bruto;
        float continentalidade = Climate.unquantizeCoord(ponto.continentalness());
        if (continentalidade >= LIMIAR_COGUMELO) return cogumelo;
        if (continentalidade >= LIMIAR_PICOS) return picos;
        if (continentalidade >= LIMIAR_PLANO) {
            float umidade = Climate.unquantizeCoord(ponto.humidity());
            if (umidade < -0.1F) return planoPedregoso;
            if (umidade > 0.25F) return planoFlorido;
            return planoCultivado;
        }
        Holder<Biome> escolhido = substituir(amostrarBase(x, y, z, amostrador, ponto));
        if (escolhido.is(BiomeTags.IS_OCEAN)) {
            escolhido = oceanoSuavizado(x, y, z, amostrador, ponto, escolhido);
        }
        if (Climate.unquantizeCoord(ponto.temperature()) < temperaturaFrio) {
            Holder<Biome> frio = escolhido.unwrapKey().map(mapaFrio::get).orElse(null);
            if (frio != null) escolhido = frio;
        }
        Holder<Biome> denso = escolhido.unwrapKey().map(mapaLonge::get).orElse(null);
        if (denso != null) {
            long bx = (long) x << 2;
            long bz = (long) z << 2;
            if (bx * bx + bz * bz >= (long) raioLonge * raioLonge) escolhido = denso;
        }
        if (territorio) {
            Holder<Biome> deTerritorio = escolhido.unwrapKey().map(mapaTerritorio::get).orElse(null);
            if (deTerritorio != null) escolhido = deTerritorio;
        }
        return escolhido;
    }

    public static double continentalidadeReal(double continentalidade) {
        return continentalidade >= LIMIAR_TERRITORIO ? continentalidade - MARCA_TERRITORIO : continentalidade;
    }

    private static Climate.TargetPoint semMarcaTerritorio(Climate.TargetPoint ponto) {
        return new Climate.TargetPoint(ponto.temperature(), ponto.humidity(),
                ponto.continentalness() - Climate.quantizeCoord(MARCA_TERRITORIO),
                ponto.erosion(), ponto.depth(), ponto.weirdness());
    }

    private Holder<Biome> oceanoSuavizado(int x, int y, int z, Climate.Sampler amostrador, Climate.TargetPoint ponto, Holder<Biome> original) {
        if (!(base instanceof MultiNoiseBiomeSource)) return original;
        Holder<Biome> suave = substituir(amostrarBase(x, y, z, amostrador, SuavizacaoOceano.suavizar(x, y, z, amostrador, ponto)));
        return suave.is(BiomeTags.IS_OCEAN) ? suave : original;
    }

    private Holder<Biome> amostrarBase(int x, int y, int z, Climate.Sampler amostrador, Climate.TargetPoint ponto) {
        if (base instanceof MultiNoiseBiomeSource multiRuido) {
            return ((AcessorFonteMultiRuido) multiRuido).aelysium$parametros().findValue(ponto);
        }
        return base.getNoiseBiome(x, y, z, amostrador);
    }

    private Stream<Holder<Biome>> biomasBase() {
        if (base instanceof MultiNoiseBiomeSource multiRuido) {
            return ((AcessorFonteMultiRuido) multiRuido).aelysium$parametros().values().stream().map(Pair::getSecond);
        }
        return base.possibleBiomes().stream();
    }

    private Holder<Biome> substituir(Holder<Biome> bioma) {
        return bioma.unwrapKey().map(mapa::get).orElse(bioma);
    }

    private Holder<Biome> isolar(Holder<Biome> bioma) {
        return bioma.unwrapKey().map(mapaIsolado::get).orElse(bioma);
    }
}
