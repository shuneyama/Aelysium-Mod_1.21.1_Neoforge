package net.aelysium.aelysiummod.sistemas.admin.listabranca;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public final class GerenciadorListaBrancaOffline {
    private static final Logger LOGGER = LoggerFactory.getLogger(GerenciadorListaBrancaOffline.class);
    private static final Pattern NOME_VALIDO = Pattern.compile("[A-Za-z0-9_]{3,16}");

    private static MinecraftServer server;

    public record Entrada(String nome, UUID uuid) {
        public UUID uuidEsperado() {
            return gerarUUIDOffline(nome);
        }

        public boolean consistente() {
            return uuid.equals(uuidEsperado());
        }
    }

    public enum Tipo {
        ADICIONADO,
        JA_EXISTE,
        ERRO
    }

    public record Resultado(Tipo tipo, Entrada entrada) {
    }

    private GerenciadorListaBrancaOffline() {
    }

    public static void inicializar(MinecraftServer mcServer) {
        server = mcServer;
    }

    public static boolean nomeValido(String nome) {
        return nome != null && NOME_VALIDO.matcher(nome).matches();
    }

    public static UUID gerarUUIDOffline(String nome) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + nome).getBytes(StandardCharsets.UTF_8));
    }

    private static UserWhiteList lista() {
        return server.getPlayerList().getWhiteList();
    }

    public static synchronized List<Entrada> entradas() {
        List<Entrada> resultado = new ArrayList<>();
        if (server == null) return resultado;
        Path arquivo = server.getServerDirectory().resolve("whitelist.json");
        if (!Files.exists(arquivo)) return resultado;
        try {
            JsonArray array = new Gson().fromJson(Files.readString(arquivo, StandardCharsets.UTF_8), JsonArray.class);
            if (array != null) {
                for (JsonElement elemento : array) {
                    JsonObject objeto = elemento.getAsJsonObject();
                    if (!objeto.has("uuid")) continue;
                    String nome = objeto.has("name") ? objeto.get("name").getAsString() : "?";
                    resultado.add(new Entrada(nome, UUID.fromString(objeto.get("uuid").getAsString())));
                }
            }
        } catch (Exception e) {
            LOGGER.error("[Aelysium-Whitelist] Erro ao ler whitelist.json", e);
        }
        resultado.sort(Comparator.comparing((Entrada e) -> e.nome().toLowerCase()).thenComparing(Entrada::nome));
        return resultado;
    }

    public static List<String> nomes() {
        List<String> nomes = new ArrayList<>();
        for (Entrada e : entradas()) nomes.add(e.nome());
        return nomes;
    }

    public static synchronized List<Entrada> procurar(String nome) {
        UUID esperado = gerarUUIDOffline(nome);
        List<Entrada> encontradas = new ArrayList<>();
        for (Entrada e : entradas()) {
            if (e.nome().equalsIgnoreCase(nome) || e.uuid().equals(esperado)) encontradas.add(e);
        }
        return encontradas;
    }

    public static synchronized Resultado adicionar(String nome) {
        if (server == null || !nomeValido(nome)) return new Resultado(Tipo.ERRO, null);
        UUID uuid = gerarUUIDOffline(nome);
        for (Entrada e : entradas()) {
            if (e.uuid().equals(uuid) && e.nome().equals(nome)) return new Resultado(Tipo.JA_EXISTE, e);
        }
        try {
            for (Entrada e : entradas()) {
                if (e.nome().equals(nome) && !e.consistente()) {
                    lista().remove(new GameProfile(e.uuid(), e.nome()));
                }
            }
            lista().add(new UserWhiteListEntry(new GameProfile(uuid, nome)));
            LOGGER.info("[Aelysium-Whitelist] Jogador adicionado: {} (UUID: {})", nome, uuid);
            return new Resultado(Tipo.ADICIONADO, new Entrada(nome, uuid));
        } catch (Exception e) {
            LOGGER.error("[Aelysium-Whitelist] Erro ao adicionar jogador", e);
            return new Resultado(Tipo.ERRO, null);
        }
    }

    public static synchronized Entrada remover(String nome) {
        if (server == null) return null;
        UUID esperado = gerarUUIDOffline(nome);
        Entrada alvo = null;
        for (Entrada e : entradas()) {
            if (e.nome().equals(nome) || e.uuid().equals(esperado)) {
                alvo = e;
                break;
            }
        }
        if (alvo == null) return null;
        try {
            lista().remove(new GameProfile(alvo.uuid(), alvo.nome()));
            LOGGER.info("[Aelysium-Whitelist] Jogador removido: {} (UUID: {})", alvo.nome(), alvo.uuid());
            return alvo;
        } catch (Exception e) {
            LOGGER.error("[Aelysium-Whitelist] Erro ao remover jogador", e);
            return null;
        }
    }
}
