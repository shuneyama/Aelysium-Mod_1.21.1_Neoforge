package net.aelysium.aelysiummod.whitelist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WhitelistOfflineManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhitelistOfflineManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String WHITELIST_FILE = "whitelist.json";

    private static File whitelistFile;
    private static MinecraftServer server;

    public static void inicializar(MinecraftServer mcServer) {
        server = mcServer;
        whitelistFile = server.getServerDirectory().resolve(WHITELIST_FILE).toFile();
        if (!whitelistFile.exists()) {
            try {
                whitelistFile.createNewFile();
                salvarLista(new JsonArray());
                LOGGER.info("[Aelysium-Whitelist] Arquivo whitelist.json criado");
            } catch (IOException e) {
                LOGGER.error("[Aelysium-Whitelist] Erro ao criar whitelist.json", e);
            }
        }
        LOGGER.info("[Aelysium-Whitelist] Sistema de whitelist offline inicializado");
    }

    public static boolean adicionar(String nome) {
        try {
            JsonArray whitelist = carregarLista();
            UUID uuidOffline = gerarUUIDOffline(nome);

            for (int i = 0; i < whitelist.size(); i++) {
                JsonObject entry = whitelist.get(i).getAsJsonObject();
                String existingName = entry.get("name").getAsString();
                if (existingName.equalsIgnoreCase(nome)) {
                    return false;
                }
            }

            JsonObject newEntry = new JsonObject();
            newEntry.addProperty("uuid", uuidOffline.toString());
            newEntry.addProperty("name", nome);
            whitelist.add(newEntry);

            salvarLista(whitelist);
            recarregarWhitelistVanilla();
            garantirEntradaVanilla(nome);
            LOGGER.info("[Aelysium-Whitelist] Jogador adicionado: {} (UUID: {})", nome, uuidOffline);
            return true;
        } catch (IOException e) {
            LOGGER.error("[Aelysium-Whitelist] Erro ao adicionar jogador", e);
            return false;
        }
    }

    public static boolean remover(String nome) {
        try {
            JsonArray whitelist = carregarLista();
            boolean removido = false;

            for (int i = 0; i < whitelist.size(); i++) {
                JsonObject entry = whitelist.get(i).getAsJsonObject();
                String existingName = entry.get("name").getAsString();
                if (existingName.equalsIgnoreCase(nome)) {
                    whitelist.remove(i);
                    removido = true;
                    break;
                }
            }

            if (removido) {
                salvarLista(whitelist);
                recarregarWhitelistVanilla();
                LOGGER.info("[Aelysium-Whitelist] Jogador removido: {}", nome);
                return true;
            }
            return false;
        } catch (IOException e) {
            LOGGER.error("[Aelysium-Whitelist] Erro ao remover jogador", e);
            return false;
        }
    }

    public static boolean estaWhitelistado(String nome) {
        try {
            JsonArray whitelist = carregarLista();
            for (int i = 0; i < whitelist.size(); i++) {
                JsonObject entry = whitelist.get(i).getAsJsonObject();
                String existingName = entry.get("name").getAsString();
                if (existingName.equalsIgnoreCase(nome)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            LOGGER.error("[Aelysium-Whitelist] Erro ao verificar whitelist", e);
            return false;
        }
    }

    public static List<String> listar() {
        try {
            JsonArray whitelist = carregarLista();
            List<String> nomes = new ArrayList<>();
            for (int i = 0; i < whitelist.size(); i++) {
                JsonObject entry = whitelist.get(i).getAsJsonObject();
                nomes.add(entry.get("name").getAsString());
            }
            Collections.sort(nomes);
            return nomes;
        } catch (IOException e) {
            LOGGER.error("[Aelysium-Whitelist] Erro ao listar whitelist", e);
            return new ArrayList<>();
        }
    }

    public static int tamanho() {
        try {
            JsonArray whitelist = carregarLista();
            return whitelist.size();
        } catch (IOException e) {
            LOGGER.error("[Aelysium-Whitelist] Erro ao obter tamanho da whitelist", e);
            return 0;
        }
    }

    public static void limpar() {
        try {
            salvarLista(new JsonArray());
            recarregarWhitelistVanilla();
            LOGGER.info("[Aelysium-Whitelist] Whitelist limpa!");
        } catch (IOException e) {
            LOGGER.error("[Aelysium-Whitelist] Erro ao limpar whitelist", e);
        }
    }

    public static UUID gerarUUIDOffline(String nome) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + nome).getBytes(StandardCharsets.UTF_8));
    }

    public static String getInfo(String nome) {
        try {
            JsonArray whitelist = carregarLista();
            for (int i = 0; i < whitelist.size(); i++) {
                JsonObject entry = whitelist.get(i).getAsJsonObject();
                String existingName = entry.get("name").getAsString();
                if (existingName.equalsIgnoreCase(nome)) {
                    String uuid = entry.get("uuid").getAsString();
                    return String.format("Nome: %s | UUID Offline: %s", existingName, uuid);
                }
            }
            return null;
        } catch (IOException e) {
            LOGGER.error("[Aelysium-Whitelist] Erro ao obter info", e);
            return null;
        }
    }

    private static JsonArray carregarLista() throws IOException {
        if (!whitelistFile.exists()) {
            return new JsonArray();
        }
        try (FileReader reader = new FileReader(whitelistFile)) {
            JsonArray array = GSON.fromJson(reader, JsonArray.class);
            return array != null ? array : new JsonArray();
        }
    }

    private static void salvarLista(JsonArray whitelist) throws IOException {
        try (FileWriter writer = new FileWriter(whitelistFile)) {
            GSON.toJson(whitelist, writer);
        }
    }

    private static void recarregarWhitelistVanilla() {
        if (server != null) {
            try {
                server.getPlayerList().reloadWhiteList();
            } catch (Exception e) {
                LOGGER.error("[Aelysium-Whitelist] Erro ao recarregar whitelist vanilla", e);
            }
        }
    }

    public static void garantirEntradaVanilla(String nome) {
        if (server == null) return;
        try {
            UUID uuid = gerarUUIDOffline(nome);
            var whitelist = server.getPlayerList().getWhiteList();
            com.mojang.authlib.GameProfile profile = new com.mojang.authlib.GameProfile(uuid, nome);
            net.minecraft.server.players.UserWhiteListEntry entry = new net.minecraft.server.players.UserWhiteListEntry(profile);
            whitelist.add(entry);
        } catch (Exception e) {
            LOGGER.error("[Aelysium-Whitelist] Erro ao garantir entrada vanilla", e);
        }
    }
}
