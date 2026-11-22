package net.aelysium.aelysiummod.sistemas.mundo.renascimento;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.aelysium.aelysiummod.nucleo.armazenamento.PastaAelysium;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class SpawnsIndividuais {

    private static final Logger LOGGER = LoggerFactory.getLogger("aelysiummod");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String ARQUIVO = "spawns_jogadores.json";

    public record Ponto(ResourceKey<Level> dimensao, double x, double y, double z, float angulo) {}

    private static final Map<UUID, Ponto> PONTOS = new TreeMap<>();

    public static synchronized void load(MinecraftServer server) {
        PONTOS.clear();
        Path arquivo = PastaAelysium.arquivo(ARQUIVO);
        if (!Files.exists(arquivo)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(arquivo, StandardCharsets.UTF_8)).getAsJsonObject();
            for (var entrada : root.entrySet()) {
                try {
                    JsonObject o = entrada.getValue().getAsJsonObject();
                    ResourceLocation loc = ResourceLocation.tryParse(o.get("dimensao").getAsString());
                    if (loc == null) continue;
                    PONTOS.put(UUID.fromString(entrada.getKey()), new Ponto(ResourceKey.create(Registries.DIMENSION, loc),
                            o.get("x").getAsDouble(), o.get("y").getAsDouble(), o.get("z").getAsDouble(),
                            o.has("angulo") ? o.get("angulo").getAsFloat() : 0.0F));
                } catch (Exception e) {
                    LOGGER.error("Entrada invalida em {}: {}", ARQUIVO, entrada.getKey(), e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Erro ao ler {}: ", ARQUIVO, e);
        }
    }

    public static synchronized Ponto obter(UUID jogador) {
        return PONTOS.get(jogador);
    }

    public static synchronized void definir(UUID jogador, Ponto ponto) {
        PONTOS.put(jogador, ponto);
        salvar();
    }

    public static synchronized boolean remover(UUID jogador) {
        boolean existia = PONTOS.remove(jogador) != null;
        if (existia) salvar();
        return existia;
    }

    private static void salvar() {
        JsonObject root = new JsonObject();
        for (var e : PONTOS.entrySet()) {
            Ponto p = e.getValue();
            JsonObject o = new JsonObject();
            o.addProperty("dimensao", p.dimensao().location().toString());
            o.addProperty("x", p.x());
            o.addProperty("y", p.y());
            o.addProperty("z", p.z());
            o.addProperty("angulo", p.angulo());
            root.add(e.getKey().toString(), o);
        }
        try {
            Path arquivo = PastaAelysium.arquivo(ARQUIVO);
            Files.createDirectories(arquivo.getParent());
            Files.writeString(arquivo, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Erro ao salvar {}: ", ARQUIVO, e);
        }
    }
}
