package net.aelysium.aelysiummod.sistemas.mundo.lithynor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FonteBiomasLithynor extends BiomeSource {

    public static final float LIMIAR_PLANO = 0.78F;
    public static final float LIMIAR_PICOS = 0.86F;
    public static final float LIMIAR_COGUMELO = 0.95F;

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
            Biome.CODEC.fieldOf("central_ocean").forGetter(s -> s.oceanoCentral),
            Codec.INT.fieldOf("central_radius").forGetter(s -> s.raioCentral),
            Troca.CODEC.listOf().fieldOf("far_replacements").forGetter(s -> s.trocasLonge),
            Codec.INT.fieldOf("far_radius").forGetter(s -> s.raioLonge),
            Troca.CODEC.listOf().fieldOf("cold_replacements").forGetter(s -> s.trocasFrio),
            Codec.FLOAT.fieldOf("cold_temperature").forGetter(s -> s.temperaturaFrio)
    ).apply(i, FonteBiomasLithynor::new));

    private final BiomeSource base;
    private final List<Troca> trocas;
    private final Holder<Biome> planoPedregoso;
    private final Holder<Biome> planoCultivado;
    private final Holder<Biome> planoFlorido;
    private final Holder<Biome> cogumelo;
    private final Holder<Biome> picos;
    private final Holder<Biome> oceanoCentral;
    private final int raioCentral;
    private final List<Troca> trocasLonge;
    private final int raioLonge;
    private final List<Troca> trocasFrio;
    private final float temperaturaFrio;
    private final Map<ResourceKey<Biome>, Holder<Biome>> mapaFrio = new HashMap<>();
    private final Map<ResourceKey<Biome>, Holder<Biome>> mapaLonge = new HashMap<>();
    private final Map<ResourceKey<Biome>, Holder<Biome>> mapa = new HashMap<>();

    public FonteBiomasLithynor(BiomeSource base, List<Troca> trocas, Holder<Biome> planoPedregoso, Holder<Biome> planoCultivado, Holder<Biome> planoFlorido, Holder<Biome> cogumelo, Holder<Biome> picos, Holder<Biome> oceanoCentral, int raioCentral, List<Troca> trocasLonge, int raioLonge, List<Troca> trocasFrio, float temperaturaFrio) {
        this.base = base;
        this.trocas = trocas;
        this.planoPedregoso = planoPedregoso;
        this.planoCultivado = planoCultivado;
        this.planoFlorido = planoFlorido;
        this.cogumelo = cogumelo;
        this.picos = picos;
        this.oceanoCentral = oceanoCentral;
        this.raioCentral = raioCentral;
        this.trocasLonge = trocasLonge;
        this.raioLonge = raioLonge;
        this.trocasFrio = trocasFrio;
        this.temperaturaFrio = temperaturaFrio;
        for (Troca troca : trocasFrio) {
            troca.de().unwrapKey().ifPresent(chave -> mapaFrio.put(chave, troca.para()));
        }
        for (Troca troca : trocasLonge) {
            troca.de().unwrapKey().ifPresent(chave -> mapaLonge.put(chave, troca.para()));
        }
        for (Troca troca : trocas) {
            troca.de().unwrapKey().ifPresent(chave -> mapa.put(chave, troca.para()));
        }
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(
                Stream.concat(Stream.concat(base.possibleBiomes().stream().map(this::substituir), trocasLonge.stream().map(Troca::para)), trocasFrio.stream().map(Troca::para)),
                Stream.of(planoPedregoso, planoCultivado, planoFlorido, cogumelo, picos, oceanoCentral)
        ).distinct();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler amostrador) {
        Climate.TargetPoint ponto = amostrador.sample(x, y, z);
        float continentalidade = Climate.unquantizeCoord(ponto.continentalness());
        if (continentalidade >= LIMIAR_COGUMELO) return cogumelo;
        if (continentalidade >= LIMIAR_PICOS) return picos;
        if (continentalidade >= LIMIAR_PLANO) {
            float umidade = Climate.unquantizeCoord(ponto.humidity());
            if (umidade < -0.1F) return planoPedregoso;
            if (umidade > 0.25F) return planoFlorido;
            return planoCultivado;
        }
        Holder<Biome> escolhido = substituir(base.getNoiseBiome(x, y, z, amostrador));
        if (escolhido.is(BiomeTags.IS_OCEAN)) {
            long bx = (long) x << 2;
            long bz = (long) z << 2;
            if (bx * bx + bz * bz < (long) raioCentral * raioCentral) return oceanoCentral;
        }
        if (Climate.unquantizeCoord(ponto.temperature()) < temperaturaFrio) {
            Holder<Biome> frio = escolhido.unwrapKey().map(mapaFrio::get).orElse(null);
            if (frio != null) escolhido = frio;
        }
        Holder<Biome> denso = escolhido.unwrapKey().map(mapaLonge::get).orElse(null);
        if (denso != null) {
            long bx = (long) x << 2;
            long bz = (long) z << 2;
            if (bx * bx + bz * bz >= (long) raioLonge * raioLonge) return denso;
        }
        return escolhido;
    }

    private Holder<Biome> substituir(Holder<Biome> bioma) {
        return bioma.unwrapKey().map(mapa::get).orElse(bioma);
    }
}
