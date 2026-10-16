package net.aelysium.aelysiummod.sistemas.social.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.aelysium.aelysiummod.nucleo.jogador.ArquivosJogador;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DadosChatJogador {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, DadosChatJogador> CACHE = new ConcurrentHashMap<>();
    private static final Map<UUID, Path> ARQUIVOS = new ConcurrentHashMap<>();
    private static Path dataFolder;

    private static final float ALTURA_JOGADOR = ConfiguracaoBalao.ALTURA_JOGADOR_BLOCOS;

    public int corTexto = 0x141414;
    public int corFundo = 0xFFFFFF;
    public int corBorda = 0x000000;
    public float altura = ConfiguracaoBalao.ALTURA_PADRAO_BLOCOS;
    public EstiloBalao estilo = EstiloBalao.ROUNDED;
    private boolean precisaMigrar = false;

    public static synchronized void setDataFolder(Path folder, Path pastaAntiga) {
        dataFolder = folder;
        CACHE.clear();
        ARQUIVOS.clear();
        try {
            Files.createDirectories(folder.resolve(ArquivosJogador.SUBPASTA_OPS));
            moverPastaAntiga(pastaAntiga, folder);
        } catch (IOException ignored) {}
        indexar(folder);
        indexar(folder.resolve(ArquivosJogador.SUBPASTA_OPS));
    }

    private static void moverPastaAntiga(Path antiga, Path nova) throws IOException {
        if (antiga == null || !Files.isDirectory(antiga)) return;
        try (var stream = Files.list(antiga)) {
            for (Path arquivo : stream.toList()) {
                Path alvo = nova.resolve(arquivo.getFileName().toString());
                if (Files.isRegularFile(arquivo) && !Files.exists(alvo)) Files.move(arquivo, alvo);
            }
        }
        try {
            Files.deleteIfExists(antiga);
        } catch (IOException ignored) {}
    }

    private static void indexar(Path pasta) {
        if (!Files.isDirectory(pasta)) return;
        try (var stream = Files.list(pasta)) {
            for (Path arquivo : stream.filter(p -> p.getFileName().toString().endsWith(".json")).toList()) {
                UUID uuid = lerUuid(arquivo);
                if (uuid != null) ARQUIVOS.put(uuid, arquivo);
            }
        } catch (IOException ignored) {}
    }

    private static UUID lerUuid(Path arquivo) {
        try {
            JsonObject root = JsonParser.parseString(Files.readString(arquivo)).getAsJsonObject();
            if (root.has("uuid")) return UUID.fromString(root.get("uuid").getAsString());
        } catch (Exception ignored) {}
        try {
            String nome = arquivo.getFileName().toString();
            return UUID.fromString(nome.substring(0, nome.length() - ".json".length()));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static DadosChatJogador get(UUID uuid) {
        DadosChatJogador existente = CACHE.get(uuid);
        if (existente != null) return existente;

        DadosChatJogador carregado = load(uuid);
        DadosChatJogador concorrente = CACHE.putIfAbsent(uuid, carregado);
        if (concorrente != null) return concorrente;

        if (carregado.precisaMigrar) {
            carregado.precisaMigrar = false;
            save(uuid);
        }
        return carregado;
    }

    public static synchronized void save(UUID uuid) {
        DadosChatJogador data = CACHE.get(uuid);
        if (data == null || dataFolder == null) return;

        JsonObject root = ArquivosJogador.cabecalho(uuid, uuid.toString().substring(0, 8));
        root.addProperty("corTexto", data.corTexto);
        root.addProperty("corFundo", data.corFundo);
        root.addProperty("corBorda", data.corBorda);
        root.addProperty("altura", data.altura);
        root.addProperty("estilo", data.estilo.name());

        try {
            Path destino = ArquivosJogador.caminho(uuid, dataFolder, ARQUIVOS, uuid.toString().substring(0, 8));
            Files.createDirectories(destino.getParent());
            Files.writeString(destino, GSON.toJson(root));
            Path anterior = ARQUIVOS.put(uuid, destino);
            if (anterior != null && !anterior.equals(destino)) Files.deleteIfExists(anterior);
        } catch (IOException ignored) {}
    }

    public static void atualizarNomes(UUID uuid) {
        if (dataFolder == null || !ARQUIVOS.containsKey(uuid)) return;
        get(uuid);
        save(uuid);
    }

    private static DadosChatJogador load(UUID uuid) {
        DadosChatJogador data = new DadosChatJogador();
        Path file = ARQUIVOS.get(uuid);
        if (dataFolder == null || file == null || !Files.exists(file)) return data;

        try {
            JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            boolean formatoAntigo = !root.has("uuid");
            if (root.has("corTexto")) data.corTexto = root.get("corTexto").getAsInt();
            if (root.has("corFundo")) data.corFundo = root.get("corFundo").getAsInt();
            if (root.has("corBorda")) data.corBorda = root.get("corBorda").getAsInt();
            if (root.has("estilo")) data.estilo = EstiloBalao.valueOf(root.get("estilo").getAsString());
            if (root.has("altura")) {
                float valor = root.get("altura").getAsFloat();
                data.altura = formatoAntigo ? valor + ALTURA_JOGADOR : valor;
            }
            data.precisaMigrar = formatoAntigo;
        } catch (Exception ignored) {}
        return data;
    }

    public static void clearCache(UUID uuid) {
        CACHE.remove(uuid);
    }
}
