package net.aelysium.aelysiummod.compatibilidade.voicechat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConfiguracaoVoz {

    private static final Logger LOGGER = LoggerFactory.getLogger(AelysiumMod.MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String MOD_VOICECHAT = "voicechat";

    public enum TipoLista {
        MUTADOS("mutados"), GLOBAL("global"), WHITELIST("whitelist");

        final String chave;

        TipoLista(String chave) {
            this.chave = chave;
        }
    }

    private static final Map<TipoLista, Map<UUID, String>> LISTAS = Map.of(
            TipoLista.MUTADOS, new ConcurrentHashMap<>(),
            TipoLista.GLOBAL, new ConcurrentHashMap<>(),
            TipoLista.WHITELIST, new ConcurrentHashMap<>()
    );

    private static Path arquivo;

    public static boolean voicechatCarregado() {
        return ModList.get().isLoaded(MOD_VOICECHAT);
    }

    @SubscribeEvent
    public void aoIniciarServidor(ServerStartingEvent event) {
        arquivo = event.getServer().getWorldPath(LevelResource.ROOT).resolve("aelysium").resolve("voice.json");
        LISTAS.values().forEach(Map::clear);
        if (!Files.exists(arquivo)) return;

        try {
            JsonObject raiz = JsonParser.parseString(Files.readString(arquivo, StandardCharsets.UTF_8)).getAsJsonObject();
            for (TipoLista tipo : TipoLista.values()) {
                if (!raiz.has(tipo.chave)) continue;
                for (Map.Entry<String, JsonElement> entrada : raiz.getAsJsonObject(tipo.chave).entrySet()) {
                    try {
                        LISTAS.get(tipo).put(UUID.fromString(entrada.getKey()), entrada.getValue().getAsString());
                    } catch (IllegalArgumentException ignorado) {
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("[Aelysium] Erro ao carregar voice.json: {}", e.getMessage());
        }
    }

    private static void salvar() {
        if (arquivo == null) return;
        JsonObject raiz = new JsonObject();
        for (TipoLista tipo : TipoLista.values()) {
            JsonObject lista = new JsonObject();
            LISTAS.get(tipo).forEach((uuid, nome) -> lista.addProperty(uuid.toString(), nome));
            raiz.add(tipo.chave, lista);
        }
        try {
            Files.createDirectories(arquivo.getParent());
            Files.writeString(arquivo, GSON.toJson(raiz), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("[Aelysium] Erro ao salvar voice.json: {}", e.getMessage());
        }
    }

    public static boolean adicionar(TipoLista tipo, UUID id, String nome) {
        boolean adicionado = LISTAS.get(tipo).put(id, nome) == null;
        if (adicionado) salvar();
        return adicionado;
    }

    public static boolean remover(TipoLista tipo, UUID id) {
        boolean removido = LISTAS.get(tipo).remove(id) != null;
        if (removido) salvar();
        return removido;
    }

    public static boolean contem(TipoLista tipo, UUID id) {
        return LISTAS.get(tipo).containsKey(id);
    }

    public static Map<UUID, String> obter(TipoLista tipo) {
        return Collections.unmodifiableMap(LISTAS.get(tipo));
    }

    public static boolean estaMutado(UUID id) {
        return contem(TipoLista.MUTADOS, id) && !contem(TipoLista.WHITELIST, id);
    }

    public static boolean temVozGlobal(UUID id) {
        return contem(TipoLista.GLOBAL, id);
    }
}
