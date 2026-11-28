package net.aelysium.aelysiummod.sistemas.mundo.lithynor;

import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SuavizacaoOceano {

    private static final int PASSO_GRADE = 16;
    private static final int RAIO_KERNEL = 40;
    private static final int PASSO_KERNEL = 8;
    private static final double SIGMA_KERNEL = 20.0;
    private static final float CONTINENTALIDADE_MINIMA = -1.04F;
    private static final float CONTINENTALIDADE_MAXIMA = -0.2F;
    private static final int TAMANHO_CACHE = 4096;
    private static final DensityFunction.SinglePointContext PONTO_IMPRESSAO = new DensityFunction.SinglePointContext(12345, 64, -6789);

    private static final int[][] DESLOCAMENTOS;
    private static final double[] PESOS;
    private static final double PESO_TOTAL;

    static {
        List<int[]> deslocamentos = new ArrayList<>();
        List<Double> pesos = new ArrayList<>();
        double soma = 0.0;
        for (int dx = -RAIO_KERNEL; dx <= RAIO_KERNEL; dx += PASSO_KERNEL) {
            for (int dz = -RAIO_KERNEL; dz <= RAIO_KERNEL; dz += PASSO_KERNEL) {
                double distanciaQuadrada = dx * dx + dz * dz;
                if (distanciaQuadrada > RAIO_KERNEL * RAIO_KERNEL) continue;
                double peso = Math.exp(-distanciaQuadrada / (2.0 * SIGMA_KERNEL * SIGMA_KERNEL));
                deslocamentos.add(new int[]{dx, dz});
                pesos.add(peso);
                soma += peso;
            }
        }
        DESLOCAMENTOS = deslocamentos.toArray(new int[0][]);
        PESOS = pesos.stream().mapToDouble(Double::doubleValue).toArray();
        PESO_TOTAL = soma;
    }

    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);

    private SuavizacaoOceano() {
    }

    public static Climate.TargetPoint suavizar(int quartX, int quartY, int quartZ, Climate.Sampler amostrador, Climate.TargetPoint ponto) {
        Cache cache = CACHE.get();
        if (cache.amostrador.get() != amostrador) cache.trocar(amostrador);
        int y = quartY << 2;
        double x = (quartX << 2) + 2.0;
        double z = (quartZ << 2) + 2.0;
        int gradeX = Mth.floor(x / PASSO_GRADE);
        int gradeZ = Mth.floor(z / PASSO_GRADE);
        double fracaoX = x / PASSO_GRADE - gradeX;
        double fracaoZ = z / PASSO_GRADE - gradeZ;
        double[] temperaturas = new double[4];
        double[] continentalidades = new double[4];
        for (int canto = 0; canto < 4; canto++) {
            int posicao = no(gradeX + (canto & 1), gradeZ + (canto >> 1), y, amostrador, cache);
            temperaturas[canto] = cache.temperaturas[posicao];
            continentalidades[canto] = cache.continentalidades[posicao];
        }
        double temperatura = Mth.lerp2(fracaoX, fracaoZ, temperaturas[0], temperaturas[1], temperaturas[2], temperaturas[3]);
        double continentalidade = Mth.lerp2(fracaoX, fracaoZ, continentalidades[0], continentalidades[1], continentalidades[2], continentalidades[3]);
        float continentalidadeLimitada = Mth.clamp((float) continentalidade, CONTINENTALIDADE_MINIMA, CONTINENTALIDADE_MAXIMA);
        return new Climate.TargetPoint(
                Climate.quantizeCoord((float) temperatura),
                ponto.humidity(),
                Climate.quantizeCoord(continentalidadeLimitada),
                ponto.erosion(),
                ponto.depth(),
                ponto.weirdness());
    }

    private static int no(int gradeX, int gradeZ, int y, Climate.Sampler amostrador, Cache cache) {
        long chave = ((long) gradeX << 32) ^ (gradeZ & 0xFFFFFFFFL);
        int posicao = (int) (((chave ^ (chave >>> 29)) * 0x9E3779B97F4A7C15L) >>> 52) & (TAMANHO_CACHE - 1);
        if (!cache.ocupado[posicao] || cache.chaves[posicao] != chave) {
            calcular(gradeX * PASSO_GRADE, y, gradeZ * PASSO_GRADE, amostrador, cache, posicao);
            cache.chaves[posicao] = chave;
            cache.ocupado[posicao] = true;
        }
        return posicao;
    }

    private static void calcular(int x, int y, int z, Climate.Sampler amostrador, Cache cache, int posicao) {
        DensityFunction temperatura = amostrador.temperature();
        DensityFunction continentalidade = amostrador.continentalness();
        double somaTemperatura = 0.0;
        double somaContinentalidade = 0.0;
        for (int i = 0; i < DESLOCAMENTOS.length; i++) {
            DensityFunction.SinglePointContext contexto =
                    new DensityFunction.SinglePointContext(x + DESLOCAMENTOS[i][0], y, z + DESLOCAMENTOS[i][1]);
            somaTemperatura += temperatura.compute(contexto) * PESOS[i];
            somaContinentalidade += FonteBiomasLithynor.continentalidadeReal(continentalidade.compute(contexto)) * PESOS[i];
        }
        cache.temperaturas[posicao] = (float) (somaTemperatura / PESO_TOTAL);
        cache.continentalidades[posicao] = (float) (somaContinentalidade / PESO_TOTAL);
    }

    private static final class Cache {
        private WeakReference<Climate.Sampler> amostrador = new WeakReference<>(null);
        private double impressaoTemperatura = Double.NaN;
        private double impressaoContinentalidade = Double.NaN;
        private final long[] chaves = new long[TAMANHO_CACHE];
        private final boolean[] ocupado = new boolean[TAMANHO_CACHE];
        private final float[] temperaturas = new float[TAMANHO_CACHE];
        private final float[] continentalidades = new float[TAMANHO_CACHE];

        private void trocar(Climate.Sampler novo) {
            amostrador = new WeakReference<>(novo);
            double temperatura = novo.temperature().compute(PONTO_IMPRESSAO);
            double continentalidade = novo.continentalness().compute(PONTO_IMPRESSAO);
            if (temperatura == impressaoTemperatura && continentalidade == impressaoContinentalidade) return;
            impressaoTemperatura = temperatura;
            impressaoContinentalidade = continentalidade;
            Arrays.fill(ocupado, false);
        }
    }
}
