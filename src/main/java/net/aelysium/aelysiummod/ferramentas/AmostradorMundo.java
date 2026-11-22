package net.aelysium.aelysiummod.ferramentas;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public final class AmostradorMundo {

    public static final String PROPRIEDADE = "aelysium.amostrador";

    private static final Logger LOGGER = LoggerFactory.getLogger("aelysiummod");
    private static final Gson GSON = new Gson();
    private static final int LIMITE_CELULAS = 700 * 700;

    private static HttpServer http;

    private AmostradorMundo() {
    }

    public static synchronized void iniciar(MinecraftServer servidor) {
        if (net.neoforged.fml.loading.FMLEnvironment.production) return;
        String porta = System.getProperty(PROPRIEDADE);
        if (porta == null || porta.isBlank() || http != null) return;
        try {
            http = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), Integer.parseInt(porta.trim())), 0);
            http.createContext("/amostrar", troca -> responder(servidor, troca));
            http.setExecutor(null);
            http.start();
            LOGGER.info("Amostrador de mundo ouvindo em http://127.0.0.1:{}/amostrar", porta.trim());
        } catch (Exception e) {
            LOGGER.error("Nao foi possivel iniciar o amostrador de mundo: ", e);
            http = null;
        }
    }

    public static synchronized void parar() {
        if (http != null) {
            http.stop(0);
            http = null;
        }
    }

    private static void responder(MinecraftServer servidor, HttpExchange troca) throws IOException {
        int status = 200;
        String corpo;
        try {
            Map<String, String> consulta = parse(troca.getRequestURI().getRawQuery());
            corpo = servidor.submit(() -> amostrar(servidor, consulta)).get();
        } catch (Exception e) {
            status = 500;
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            LOGGER.error("Falha ao amostrar o mundo: ", causa);
            Map<String, Object> erro = new HashMap<>();
            erro.put("error", String.valueOf(causa.getMessage()));
            corpo = GSON.toJson(erro);
        }
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        troca.sendResponseHeaders(status, bytes.length);
        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(bytes);
        }
    }

    private static Map<String, String> parse(String bruto) {
        Map<String, String> mapa = new HashMap<>();
        if (bruto == null) return mapa;
        for (String par : bruto.split("&")) {
            int i = par.indexOf('=');
            if (i <= 0) continue;
            mapa.put(URLDecoder.decode(par.substring(0, i), StandardCharsets.UTF_8), URLDecoder.decode(par.substring(i + 1), StandardCharsets.UTF_8));
        }
        return mapa;
    }

    private static String amostrar(MinecraftServer servidor, Map<String, String> p) {
        long seed = Long.parseLong(p.getOrDefault("seed", "0"));
        int origemX = Integer.parseInt(p.getOrDefault("origin_x", "0"));
        int origemZ = Integer.parseInt(p.getOrDefault("origin_z", "0"));
        int largura = Integer.parseInt(p.getOrDefault("width", "128"));
        int altura = Integer.parseInt(p.getOrDefault("height", String.valueOf(largura)));
        int passo = Math.max(1, Integer.parseInt(p.getOrDefault("step", "16")));
        if (largura < 1 || altura < 1 || largura * altura > LIMITE_CELULAS) {
            throw new IllegalArgumentException("Tamanho invalido (maximo " + LIMITE_CELULAS + " celulas)");
        }
        ResourceLocation id = ResourceLocation.parse(p.getOrDefault("dim", "aelysium:lithynor"));
        ServerLevel nivel = servidor.getLevel(ResourceKey.create(Registries.DIMENSION, id));
        if (nivel == null) throw new IllegalArgumentException("Dimensao nao encontrada: " + id);
        ChunkGenerator gerador = nivel.getChunkSource().getGenerator();
        if (!(gerador instanceof NoiseBasedChunkGenerator ruido)) {
            throw new IllegalArgumentException("A dimensao " + id + " nao usa o gerador de ruido");
        }

        RandomState estado = RandomState.create(ruido.generatorSettings().value(), servidor.registryAccess().lookupOrThrow(Registries.NOISE), seed);
        int nivelMar = ruido.getSeaLevel();
        int minY = nivel.getMinBuildHeight();
        int maxY = nivel.getMaxBuildHeight();
        int quartY = QuartPos.fromBlock(nivelMar);

        boolean completo = !"0".equals(p.getOrDefault("full", "1"));
        ConcurrentHashMap<String, Integer> paleta = new ConcurrentHashMap<>();
        AtomicInteger proximo = new AtomicInteger();
        int[] biomas = new int[largura * altura];
        int[] fundo = new int[largura * altura];
        int[] superficie = new int[largura * altura];

        IntStream.range(0, largura * altura).parallel().forEach(indice -> {
            int x = origemX + (indice % largura) * passo + passo / 2;
            int z = origemZ + (indice / largura) * passo + passo / 2;
            Holder<Biome> bioma = ruido.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(x), quartY, QuartPos.fromBlock(z), estado.sampler());
            String nome = bioma.unwrapKey().map(k -> k.location().toString()).orElse("unknown");
            biomas[indice] = paleta.computeIfAbsent(nome, k -> proximo.getAndIncrement());
            if (!completo) return;
            NoiseColumn coluna = ruido.getBaseColumn(x, z, nivel, estado);
            int topoSolido = minY;
            int topoAgua = minY;
            for (int y = maxY - 1; y >= minY; y--) {
                BlockState bloco = coluna.getBlock(y);
                if (bloco.isAir()) continue;
                if (!bloco.getFluidState().isEmpty()) {
                    if (topoAgua == minY) topoAgua = y;
                    continue;
                }
                topoSolido = y;
                break;
            }
            fundo[indice] = topoSolido;
            superficie[indice] = Math.max(topoSolido, topoAgua);
        });

        Map<String, Object> saida = new LinkedHashMap<>();
        saida.put("seed", seed);
        saida.put("dimension", id.toString());
        saida.put("width", largura);
        saida.put("height", altura);
        saida.put("origin_x", origemX);
        saida.put("origin_z", origemZ);
        saida.put("step", passo);
        saida.put("sea_level", nivelMar);
        String[] lista = new String[paleta.size()];
        paleta.forEach((nome, posicao) -> lista[posicao] = nome);
        saida.put("palette", lista);
        saida.put("biome_index", biomas);
        if (completo) saida.put("floor", fundo);
        if (completo) saida.put("surface", superficie);
        return GSON.toJson(saida);
    }
}
