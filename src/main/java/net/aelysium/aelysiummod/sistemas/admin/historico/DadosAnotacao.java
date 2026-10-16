package net.aelysium.aelysiummod.sistemas.admin.historico;

import com.google.gson.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DadosAnotacao {

    public record Anotacao(
            UUID alvoUuid,
            String alvoNome,
            String tipo,
            String comentario,
            long timestamp,
            UUID responsavelUuid,
            String responsavelNome,
            long tempoJogadoMinutos
    ) {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path pastaAnotacoes;
    private static final Map<UUID, Path> arquivos = new ConcurrentHashMap<>();
    private static final Map<UUID, List<Anotacao>> todasAnotacoes = new ConcurrentHashMap<>();

    public static synchronized void inicializar(Path pastaDasAnotacoes) {
        pastaAnotacoes = pastaDasAnotacoes;
        todasAnotacoes.clear();
        arquivos.clear();
        try {
            Files.createDirectories(pastaAnotacoes);
            carregarTodos();
            for (UUID uuid : new ArrayList<>(todasAnotacoes.keySet())) salvarJogador(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void adicionarAnotacao(Anotacao anotacao) {
        todasAnotacoes.computeIfAbsent(anotacao.alvoUuid(), k -> new ArrayList<>()).add(anotacao);
        salvarJogador(anotacao.alvoUuid());
    }

    public static synchronized boolean removerAnotacao(UUID uuid, int indiceBase0) {
        List<Anotacao> lista = todasAnotacoes.get(uuid);
        if (lista == null || indiceBase0 < 0 || indiceBase0 >= lista.size()) return false;
        lista.remove(indiceBase0);
        salvarJogador(uuid);
        return true;
    }

    public static List<Anotacao> getAnotacoes(UUID uuid) {
        return new ArrayList<>(todasAnotacoes.getOrDefault(uuid, List.of()));
    }

    public static int contarPorTipo(UUID uuid, String tipo) {
        return (int) todasAnotacoes.getOrDefault(uuid, List.of()).stream()
                .filter(a -> a.tipo().equalsIgnoreCase(tipo))
                .count();
    }

    public static int totalAnotacoes(UUID uuid) {
        return todasAnotacoes.getOrDefault(uuid, List.of()).size();
    }

    public static Map<UUID, List<Anotacao>> getTodas() {
        return Collections.unmodifiableMap(todasAnotacoes);
    }

    public static synchronized void atualizarNomes(UUID uuid) {
        if (pastaAnotacoes != null && todasAnotacoes.containsKey(uuid)) salvarJogador(uuid);
    }

    private static void carregarTodos() {
        try {
            if (!Files.exists(pastaAnotacoes)) return;
            try (var arquivosJson = Files.list(pastaAnotacoes)) {
                arquivosJson
                        .filter(p -> p.getFileName().toString().endsWith(".json"))
                        .forEach(DadosAnotacao::carregarArquivo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void carregarArquivo(Path arquivo) {
        try {
            String conteudo = Files.readString(arquivo, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(conteudo).getAsJsonObject();
            UUID uuid = UUID.fromString(root.get("uuid").getAsString());
            String nomeAlvo = root.has("nome") ? root.get("nome").getAsString() : null;

            List<Anotacao> lista = new ArrayList<>();
            for (JsonElement el : root.getAsJsonArray("anotacoes")) {
                JsonObject obj = el.getAsJsonObject();
                if (nomeAlvo == null && obj.has("alvo_nome")) nomeAlvo = obj.get("alvo_nome").getAsString();

                long momento;
                UUID respUuid;
                String respNome;
                if (obj.has("em")) {
                    momento = FormatoDataHistorico.interpretar(obj.get("em"));
                    String por = obj.get("por").getAsString();
                    int corte = por.lastIndexOf(" - ");
                    respNome = corte >= 0 ? por.substring(0, corte) : por;
                    respUuid = null;
                    if (corte >= 0) {
                        try {
                            respUuid = UUID.fromString(por.substring(corte + 3).trim());
                        } catch (IllegalArgumentException ignored) {}
                    }
                } else {
                    momento = obj.get("timestamp").getAsLong();
                    String respStr = obj.get("responsavel_uuid").getAsString();
                    respUuid = respStr.isEmpty() ? null : UUID.fromString(respStr);
                    respNome = obj.get("responsavel_nome").getAsString();
                }

                long minutos = 0;
                if (obj.has("tempoJogadoMinutos")) {
                    try {
                        minutos = Long.parseLong(obj.get("tempoJogadoMinutos").getAsString());
                    } catch (NumberFormatException ignored) {}
                }
                lista.add(new Anotacao(uuid, nomeAlvo == null ? uuid.toString().substring(0, 8) : nomeAlvo,
                        obj.get("tipo").getAsString(), obj.get("comentario").getAsString(),
                        momento, respUuid, respNome, minutos));
            }
            todasAnotacoes.put(uuid, lista);
            arquivos.put(uuid, arquivo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void salvarJogador(UUID uuid) {
        List<Anotacao> lista = todasAnotacoes.getOrDefault(uuid, List.of());
        PerfilHistorico perfil = DadosHistorico.getPerfil(uuid);
        String nomeAtual = perfil != null ? perfil.nomeAtual()
                : (lista.isEmpty() ? uuid.toString().substring(0, 8) : lista.get(lista.size() - 1).alvoNome());

        JsonObject root = new JsonObject();
        root.addProperty("uuid", uuid.toString());
        root.addProperty("nome", nomeAtual);
        root.addProperty("nomeMinecraft", perfil != null ? perfil.nomeMinecraft : nomeAtual);

        JsonArray nomes = new JsonArray();
        if (perfil != null) {
            for (PerfilHistorico.NomeHistorico n : perfil.nomes) {
                JsonObject obj = new JsonObject();
                obj.addProperty("nome", n.nome());
                obj.addProperty("tipo", n.tipo());
                obj.addProperty("desde", n.desde());
                nomes.add(obj);
            }
        }
        root.add("historicoNomes", nomes);

        JsonArray arr = new JsonArray();
        for (Anotacao a : lista) {
            JsonObject obj = new JsonObject();
            obj.addProperty("tipo", a.tipo());
            obj.addProperty("comentario", a.comentario());
            obj.addProperty("em", FormatoDataHistorico.formatar(a.timestamp()));
            obj.addProperty("por", a.responsavelUuid() != null ? a.responsavelNome() + " - " + a.responsavelUuid() : a.responsavelNome());
            obj.addProperty("tempoJogadoMinutos", String.valueOf(a.tempoJogadoMinutos()));
            arr.add(obj);
        }
        root.add("anotacoes", arr);

        Path destino = caminhoPara(uuid, nomeAtual);
        try {
            Files.createDirectories(destino.getParent());
            Files.writeString(destino, GSON.toJson(root), StandardCharsets.UTF_8);
            Path anterior = arquivos.put(uuid, destino);
            if (anterior != null && !anterior.equals(destino)) Files.deleteIfExists(anterior);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path caminhoPara(UUID uuid, String nome) {
        String base = nome.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        while (base.endsWith(".") || base.endsWith(" ")) base = base.substring(0, base.length() - 1);
        if (base.isBlank()) base = "desconhecido";

        Path candidato = pastaAnotacoes.resolve(base + ".json");
        for (var e : arquivos.entrySet()) {
            if (!e.getKey().equals(uuid) && e.getValue().toString().equalsIgnoreCase(candidato.toString())) {
                return pastaAnotacoes.resolve(base + "_" + uuid.toString().substring(0, 8) + ".json");
            }
        }
        return candidato;
    }

    public static Path getPastaAnotacoes() {
        return pastaAnotacoes;
    }
}
