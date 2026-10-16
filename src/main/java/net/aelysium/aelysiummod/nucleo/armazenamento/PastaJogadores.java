package net.aelysium.aelysiummod.nucleo.armazenamento;

import com.google.gson.*;
import net.aelysium.aelysiummod.nucleo.jogador.ArquivosJogador;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

class PastaJogadores {

    private static final Logger LOGGER = LoggerFactory.getLogger("AelysiumArmazenamento");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<String> CHAVES_DO_TOPO = Set.of("uuid", "nome", "nomeMinecraft", "historicoNomes");
    private static final List<String> ORDEM_DAS_SECOES = List.of("apelido", "pronomes", "raca", "deus", "insignia", "live");
    private static final Map<Path, PastaJogadores> POR_PASTA = new HashMap<>();

    private final Path pasta;
    private final Map<UUID, JsonObject> secoes = new LinkedHashMap<>();
    private final Map<UUID, Path> arquivos = new HashMap<>();
    private final Map<UUID, String> ultimoConteudo = new HashMap<>();

    private PastaJogadores(Path pasta) {
        this.pasta = pasta;
    }

    static synchronized PastaJogadores de(Path pasta) {
        return POR_PASTA.computeIfAbsent(pasta, PastaJogadores::new);
    }

    private boolean carregada;

    synchronized void recarregarSeVazia() {
        if (!carregada) recarregar();
    }

    synchronized void recarregar() {
        carregada = true;
        secoes.clear();
        arquivos.clear();
        ultimoConteudo.clear();
        for (Path dir : List.of(pasta, pasta.resolve(ArquivosJogador.SUBPASTA_OPS))) {
            if (!Files.isDirectory(dir)) continue;
            try (var stream = Files.list(dir)) {
                for (Path arquivo : stream.filter(p -> p.getFileName().toString().endsWith(".json")).toList()) {
                    try {
                        String texto = Files.readString(arquivo, StandardCharsets.UTF_8);
                        JsonObject root = JsonParser.parseString(texto).getAsJsonObject();
                        UUID uuid = UUID.fromString(root.get("uuid").getAsString());
                        JsonObject dados = new JsonObject();
                        for (var entrada : root.entrySet()) {
                            if (!CHAVES_DO_TOPO.contains(entrada.getKey())) dados.add(entrada.getKey(), entrada.getValue());
                        }
                        secoes.put(uuid, dados);
                        arquivos.put(uuid, arquivo);
                        ultimoConteudo.put(uuid, texto);
                    } catch (Exception e) {
                        LOGGER.error("Erro ao ler {}: ", arquivo, e);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Erro ao listar {}: ", dir, e);
            }
        }
    }

    synchronized void importarDe(Path antiga, MinecraftServer server) {
        if (!Files.isDirectory(antiga)) return;
        Set<UUID> alterados = new LinkedHashSet<>();
        for (Path dir : List.of(antiga, antiga.resolve(ArquivosJogador.SUBPASTA_OPS))) {
            if (!Files.isDirectory(dir)) continue;
            try (var stream = Files.list(dir)) {
                for (Path arquivo : stream.filter(p -> p.getFileName().toString().endsWith(".json")).toList()) {
                    try {
                        JsonObject root = JsonParser.parseString(Files.readString(arquivo, StandardCharsets.UTF_8)).getAsJsonObject();
                        UUID uuid = UUID.fromString(root.get("uuid").getAsString());
                        for (var entrada : root.entrySet()) {
                            if (CHAVES_DO_TOPO.contains(entrada.getKey())) continue;
                            if (secao(uuid, entrada.getKey()) == null && definir(uuid, entrada.getKey(), entrada.getValue())) alterados.add(uuid);
                        }
                        Files.deleteIfExists(arquivo);
                    } catch (Exception e) {
                        LOGGER.error("Erro ao importar {}: ", arquivo, e);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Erro ao listar {}: ", dir, e);
            }
        }
        for (UUID uuid : alterados) gravar(uuid, server);
        try {
            Files.deleteIfExists(antiga.resolve(ArquivosJogador.SUBPASTA_OPS));
            Files.deleteIfExists(antiga);
        } catch (IOException ignored) {}
    }

    synchronized Set<UUID> jogadores() {
        return new LinkedHashSet<>(secoes.keySet());
    }

    synchronized JsonElement secao(UUID uuid, String chave) {
        JsonObject dados = secoes.get(uuid);
        return dados == null ? null : dados.get(chave);
    }

    synchronized boolean definir(UUID uuid, String chave, JsonElement valor) {
        JsonObject dados = secoes.get(uuid);
        if (valor == null) {
            if (dados == null || !dados.has(chave)) return false;
            dados.remove(chave);
            return true;
        }
        if (dados == null) {
            dados = new JsonObject();
            secoes.put(uuid, dados);
        }
        if (valor.equals(dados.get(chave))) return false;
        dados.add(chave, valor);
        return true;
    }

    synchronized boolean gravar(UUID uuid, MinecraftServer server) {
        JsonObject dados = secoes.get(uuid);
        Path atual = arquivos.get(uuid);
        try {
            if (dados == null || dados.size() == 0) {
                secoes.remove(uuid);
                ultimoConteudo.remove(uuid);
                if (atual != null) {
                    Files.deleteIfExists(atual);
                    arquivos.remove(uuid);
                }
                return true;
            }

            String reserva = nomeReserva(uuid, server);
            JsonObject root = ArquivosJogador.cabecalho(uuid, reserva);
            for (String chave : ORDEM_DAS_SECOES) {
                if (dados.has(chave)) root.add(chave, dados.get(chave));
            }
            for (var entrada : dados.entrySet()) {
                if (!ORDEM_DAS_SECOES.contains(entrada.getKey())) root.add(entrada.getKey(), entrada.getValue());
            }
            String texto = GSON.toJson(root);

            Path alvo = ArquivosJogador.caminho(uuid, pasta, arquivos, reserva);
            if (alvo.equals(atual) && texto.equals(ultimoConteudo.get(uuid)) && Files.exists(alvo)) return true;

            Files.createDirectories(alvo.getParent());
            Files.writeString(alvo, texto, StandardCharsets.UTF_8);
            arquivos.put(uuid, alvo);
            ultimoConteudo.put(uuid, texto);
            if (atual != null && !atual.equals(alvo)) Files.deleteIfExists(atual);
            return true;
        } catch (IOException e) {
            LOGGER.error("Erro ao gravar o arquivo de {}: ", uuid, e);
            return false;
        }
    }

    private static String nomeReserva(UUID uuid, MinecraftServer server) {
        try {
            var perfil = server.getProfileCache().get(uuid);
            if (perfil.isPresent() && perfil.get().getName() != null) return perfil.get().getName();
        } catch (Exception ignored) {}
        return uuid.toString().substring(0, 8);
    }
}
