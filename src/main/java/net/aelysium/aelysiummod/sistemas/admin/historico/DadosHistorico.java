package net.aelysium.aelysiummod.sistemas.admin.historico;

import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DadosHistorico {

    public record Sessao(
            UUID uuid,
            String nomeJogador,
            long loginMs,
            long logoutMs,
            String ip
    ) {
        public long duracaoMs() {
            long fim = logoutMs > 0 ? logoutMs : System.currentTimeMillis();
            return Math.max(0, fim - loginMs);
        }

        public double duracaoHoras() {
            return duracaoMs() / 3_600_000.0;
        }

        public boolean finalizada() {
            return logoutMs > 0;
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final long VALIDADE_AUTOR_OP_MS = 60_000;

    private static Path pastaBase;
    private static Path pastaData;
    private static Path pastaOps;
    private static Path pastaMods;
    private static Path pastaModsOps;
    private static final Map<UUID, List<Sessao>> todasSessoes = new LinkedHashMap<>();
    private static final Map<UUID, Sessao> sessoesAtivas = new ConcurrentHashMap<>();
    private static final Map<UUID, String> nomesVanilla = new LinkedHashMap<>();
    private static final Map<UUID, Long> temposVanilla = new LinkedHashMap<>();
    private static final Map<UUID, PerfilHistorico> perfis = new LinkedHashMap<>();
    private static final Map<UUID, Path> arquivos = new HashMap<>();
    private static final Map<UUID, Path> arquivosMods = new HashMap<>();
    private static final Map<String, String[]> autoresOp = new HashMap<>();

    public static synchronized void inicializar(MinecraftServer server) {
        pastaBase = server.getWorldPath(LevelResource.ROOT).resolve("aelysium");
        pastaData = pastaBase.resolve("historico_sessoes");
        pastaOps = pastaData.resolve("ops");
        pastaMods = pastaBase.resolve("historico_mods");
        pastaModsOps = pastaMods.resolve("ops");
        todasSessoes.clear();
        sessoesAtivas.clear();
        nomesVanilla.clear();
        temposVanilla.clear();
        perfis.clear();
        arquivos.clear();
        arquivosMods.clear();
        try {
            Files.createDirectories(pastaOps);
            Files.createDirectories(pastaModsOps);
            carregarPasta(pastaData);
            carregarPasta(pastaOps);
            carregarPastaMods(pastaMods);
            carregarPastaMods(pastaModsOps);
            migrarPastaAntiga(server);
            importarJogadoresVanilla(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void registrarLogin(ServerPlayer player, String ip) {
        UUID uuid = player.getUUID();
        String nome = player.getName().getString();
        Sessao sessao = new Sessao(uuid, nome, System.currentTimeMillis(), 0, ip);
        sessoesAtivas.put(uuid, sessao);
        todasSessoes.computeIfAbsent(uuid, k -> new ArrayList<>());
        nomesVanilla.remove(uuid);
        temposVanilla.remove(uuid);

        PerfilHistorico perfil = perfis.computeIfAbsent(uuid, PerfilHistorico::new);
        String agora = FormatoDataHistorico.agora();
        perfil.atualizarNomes(nome, apelidoDe(player.getServer(), uuid), agora);
        boolean ehOp = player.getServer() != null && player.getServer().getPlayerList().isOp(player.getGameProfile());
        if (perfil.opHistorico.isEmpty()) {
            if (ehOp) perfil.registrarOp(true, "desconhecido", agora);
            else perfil.op = false;
        }
        salvarJogador(uuid);
        DadosAnotacao.atualizarNomes(uuid);
        net.aelysium.aelysiummod.sistemas.social.chat.DadosChatJogador.atualizarNomes(uuid);
        net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos.aoJogadorMudou(uuid);
    }

    public static synchronized void registrarLogout(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Sessao ativa = sessoesAtivas.remove(uuid);
        if (ativa == null) return;
        Sessao finalizada = new Sessao(uuid, ativa.nomeJogador(), ativa.loginMs(), System.currentTimeMillis(), ativa.ip());
        todasSessoes.computeIfAbsent(uuid, k -> new ArrayList<>()).add(finalizada);
        salvarJogador(uuid);
    }

    public static synchronized void registrarMods(ServerPlayer player, String minecraft, String loader,
                                                  String loaderVersao, List<ModCliente> mods) {
        if (pastaData == null) return;
        UUID uuid = player.getUUID();
        PerfilHistorico perfil = perfis.computeIfAbsent(uuid, PerfilHistorico::new);
        if (perfil.nomeMinecraft.isBlank()) perfil.atualizarNomes(player.getName().getString(), apelidoDe(player.getServer(), uuid), FormatoDataHistorico.agora());
        perfil.atualizarMods(minecraft, loader, loaderVersao, mods, FormatoDataHistorico.agora());
        salvarJogador(uuid);
    }

    public static synchronized void registrarAutorOp(String alvo, String autor) {
        autoresOp.put(alvo.toLowerCase(), new String[]{autor, String.valueOf(System.currentTimeMillis())});
    }

    public static synchronized void aoApelidoAlterado(MinecraftServer server, UUID uuid) {
        if (pastaData == null) return;
        PerfilHistorico perfil = perfis.get(uuid);
        if (perfil == null) return;

        int totalNomes = perfil.nomes.size();
        String antes = perfil.nomeAtual();
        perfil.atualizarNomes(perfil.nomeMinecraft, apelidoDe(server, uuid), FormatoDataHistorico.agora());
        if (antes.equals(perfil.nomeAtual()) && totalNomes == perfil.nomes.size()) return;

        salvarJogador(uuid);
        DadosAnotacao.atualizarNomes(uuid);
        net.aelysium.aelysiummod.sistemas.social.chat.DadosChatJogador.atualizarNomes(uuid);
        net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos.aoJogadorMudou(uuid);
    }

    public static synchronized void manutencao(MinecraftServer server) {
        if (pastaData == null) return;


        String agora = FormatoDataHistorico.agora();
        for (PerfilHistorico perfil : new ArrayList<>(perfis.values())) {
            boolean alterado = false;

            boolean ehOp = server.getPlayerList().isOp(new com.mojang.authlib.GameProfile(perfil.uuid, perfil.nomeMinecraft));
            if (ehOp != perfil.op) {
                perfil.registrarOp(ehOp, autorDaMudancaOp(perfil.nomeMinecraft), agora);
                alterado = true;
            }

            if (alterado) {
                salvarJogador(perfil.uuid);
                DadosAnotacao.atualizarNomes(perfil.uuid);
                net.aelysium.aelysiummod.sistemas.social.chat.DadosChatJogador.atualizarNomes(perfil.uuid);
                net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos.aoJogadorMudou(perfil.uuid);
            }
        }
        autoresOp.values().removeIf(v -> System.currentTimeMillis() - Long.parseLong(v[1]) > VALIDADE_AUTOR_OP_MS);
    }

    private static String autorDaMudancaOp(String alvo) {
        String[] registro = autoresOp.get(alvo.toLowerCase());
        if (registro == null || System.currentTimeMillis() - Long.parseLong(registro[1]) > VALIDADE_AUTOR_OP_MS) {
            return "desconhecido";
        }
        return registro[0];
    }

    private static String apelidoDe(MinecraftServer server, UUID uuid) {
        if (server == null) return null;
        try {
            return net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador.apelido().apelido(server, uuid);
        } catch (Exception e) {
            return null;
        }
    }

    public static List<UUID> getJogadoresOrdenadosPorHoras() {
        Set<UUID> todos = new LinkedHashSet<>();
        todos.addAll(todasSessoes.keySet());
        todos.addAll(sessoesAtivas.keySet());
        todos.addAll(nomesVanilla.keySet());
        return todos.stream()
                .sorted(Comparator.comparingDouble(DadosHistorico::getTotalHoras).reversed())
                .collect(Collectors.toList());
    }

    public static String getNome(UUID uuid) {
        List<Sessao> sessoes = todasSessoes.get(uuid);
        if (sessoes != null && !sessoes.isEmpty()) {
            return sessoes.get(sessoes.size() - 1).nomeJogador();
        }
        Sessao ativa = sessoesAtivas.get(uuid);
        if (ativa != null) return ativa.nomeJogador();
        String vanilla = nomesVanilla.get(uuid);
        return vanilla != null ? vanilla : uuid.toString().substring(0, 8);
    }

    public static double getTotalHoras(UUID uuid) {
        double total = 0;
        List<Sessao> sessoes = todasSessoes.get(uuid);
        if (sessoes != null) {
            for (Sessao s : sessoes) total += s.duracaoHoras();
        }
        Sessao ativa = sessoesAtivas.get(uuid);
        if (ativa != null) total += ativa.duracaoHoras();
        Long tempoVanilla = temposVanilla.get(uuid);
        if (tempoVanilla != null) total += tempoVanilla / 3_600_000.0;
        return total;
    }

    public static int getTotalSessoes(UUID uuid) {
        int total = todasSessoes.getOrDefault(uuid, List.of()).size();
        if (sessoesAtivas.containsKey(uuid)) total++;
        return total;
    }

    public static List<Sessao> getSessoes(UUID uuid) {
        List<Sessao> lista = new ArrayList<>(todasSessoes.getOrDefault(uuid, List.of()));
        Sessao ativa = sessoesAtivas.get(uuid);
        if (ativa != null) lista.add(ativa);
        return lista;
    }

    public static UUID resolverIdentificador(String identificador) {
        try {
            return UUID.fromString(identificador);
        } catch (IllegalArgumentException ignored) {}
        for (UUID uuid : todasSessoes.keySet()) {
            if (getNome(uuid).equalsIgnoreCase(identificador)) return uuid;
        }
        for (UUID uuid : sessoesAtivas.keySet()) {
            if (getNome(uuid).equalsIgnoreCase(identificador)) return uuid;
        }
        for (UUID uuid : nomesVanilla.keySet()) {
            if (getNome(uuid).equalsIgnoreCase(identificador)) return uuid;
        }
        return null;
    }

    public static Set<String> getNomesTodos() {
        Set<UUID> todos = new LinkedHashSet<>();
        todos.addAll(todasSessoes.keySet());
        todos.addAll(sessoesAtivas.keySet());
        todos.addAll(nomesVanilla.keySet());
        return todos.stream().map(DadosHistorico::getNome).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static double getMediaDiaria(UUID uuid) {
        List<Sessao> sessoes = getSessoes(uuid);
        if (sessoes.isEmpty()) return 0;
        Map<LocalDate, Double> porDia = new TreeMap<>();
        for (Sessao s : sessoes) {
            LocalDate dia = Instant.ofEpochMilli(s.loginMs()).atZone(ZoneId.systemDefault()).toLocalDate();
            porDia.merge(dia, s.duracaoHoras(), Double::sum);
        }
        return porDia.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public static double getMediaSemanal(UUID uuid) {
        List<Sessao> sessoes = getSessoes(uuid);
        if (sessoes.isEmpty()) return 0;
        WeekFields wf = WeekFields.of(Locale.getDefault());
        Map<String, Double> porSemana = new TreeMap<>();
        for (Sessao s : sessoes) {
            LocalDate dia = Instant.ofEpochMilli(s.loginMs()).atZone(ZoneId.systemDefault()).toLocalDate();
            String chave = dia.getYear() + "-S" + dia.get(wf.weekOfWeekBasedYear());
            porSemana.merge(chave, s.duracaoHoras(), Double::sum);
        }
        return porSemana.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public static String obterIpJogador(ServerPlayer player) {
        try {
            java.lang.reflect.Field f = net.minecraft.server.network.ServerCommonPacketListenerImpl.class.getDeclaredField("connection");
            f.setAccessible(true);
            if (f.get(player.connection) instanceof net.minecraft.network.Connection conn) {
                if (conn.getRemoteAddress() instanceof InetSocketAddress inet) {
                    return inet.getAddress().getHostAddress();
                }
            }
        } catch (Exception ignored) {}
        return "desconhecido";
    }

    private static void importarJogadoresVanilla(MinecraftServer server) {
        Path playerDataPath = server.getWorldPath(LevelResource.ROOT).resolve("playerdata");
        Path statsPath = server.getWorldPath(LevelResource.ROOT).resolve("stats");
        if (!Files.exists(playerDataPath)) return;
        try {
            Files.list(playerDataPath)
                    .filter(p -> p.getFileName().toString().endsWith(".dat"))
                    .forEach(p -> {
                        String filename = p.getFileName().toString();
                        String uuidStr = filename.substring(0, filename.length() - 4);
                        try {
                            UUID uuid = UUID.fromString(uuidStr);
                            if (todasSessoes.containsKey(uuid) || sessoesAtivas.containsKey(uuid)) return;
                            server.getProfileCache().get(uuid).ifPresentOrElse(
                                    profile -> nomesVanilla.put(uuid, profile.getName() != null ? profile.getName() : uuidStr.substring(0, 8)),
                                    () -> nomesVanilla.put(uuid, uuidStr.substring(0, 8))
                            );
                            long tempoMs = lerTempoVanilla(statsPath, uuid);
                            if (tempoMs > 0) temposVanilla.put(uuid, tempoMs);
                        } catch (IllegalArgumentException ignored) {}
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long lerTempoVanilla(Path statsPath, UUID uuid) {
        Path arquivo = statsPath.resolve(uuid + ".json");
        if (!Files.exists(arquivo)) return 0;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(arquivo, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject stats = root.getAsJsonObject("stats");
            if (stats == null) return 0;
            JsonObject custom = stats.getAsJsonObject("minecraft:custom");
            if (custom == null) return 0;
            long ticks = 0;
            if (custom.has("minecraft:play_time")) {
                ticks = custom.get("minecraft:play_time").getAsLong();
            } else if (custom.has("minecraft:play_one_minute")) {
                ticks = custom.get("minecraft:play_one_minute").getAsLong();
            }
            return ticks * 50L;
        } catch (Exception ignored) {}
        return 0;
    }


    private static void carregarPasta(Path pasta) {
        try {
            if (!Files.exists(pasta)) return;
            try (var arquivosJson = Files.list(pasta)) {
                arquivosJson
                        .filter(p -> p.getFileName().toString().endsWith(".json"))
                        .forEach(DadosHistorico::carregarArquivo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void carregarPastaMods(Path pasta) {
        try {
            if (!Files.exists(pasta)) return;
            try (var arquivosJson = Files.list(pasta)) {
                arquivosJson
                        .filter(p -> p.getFileName().toString().endsWith(".json"))
                        .forEach(DadosHistorico::carregarArquivoMods);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void carregarArquivoMods(Path arquivo) {
        try {
            JsonObject root = JsonParser.parseString(Files.readString(arquivo, StandardCharsets.UTF_8)).getAsJsonObject();
            UUID uuid = UUID.fromString(root.get("uuid").getAsString());
            PerfilHistorico lido = PerfilHistorico.deJson(uuid, root);
            PerfilHistorico perfil = perfis.computeIfAbsent(uuid, PerfilHistorico::new);
            perfil.copiarMods(lido);
            if (perfil.nomeMinecraft.isBlank()) perfil.nomeMinecraft = lido.nomeMinecraft;
            if (perfil.nomes.isEmpty()) perfil.nomes.addAll(lido.nomes);
            arquivosMods.put(uuid, arquivo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static UUID carregarArquivo(Path arquivo) {
        try {
            String conteudo = Files.readString(arquivo, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(conteudo).getAsJsonObject();
            UUID uuid = UUID.fromString(root.get("uuid").getAsString());
            if (todasSessoes.containsKey(uuid)) return null;

            List<Sessao> lista = new ArrayList<>();
            for (JsonElement el : root.getAsJsonArray("sessoes")) {
                JsonObject obj = el.getAsJsonObject();
                long login = FormatoDataHistorico.interpretar(obj.get("login"));
                long logout = FormatoDataHistorico.interpretar(obj.get("logout"));
                if (logout == 0) logout = login;
                lista.add(new Sessao(uuid, obj.get("nome").getAsString(), login, logout, obj.get("ip").getAsString()));
            }
            todasSessoes.put(uuid, lista);

            PerfilHistorico perfil = PerfilHistorico.deJson(uuid, root);
            if (perfil.nomeMinecraft.isBlank() && !lista.isEmpty()) perfil.nomeMinecraft = lista.get(lista.size() - 1).nomeJogador();
            if (perfil.nomes.isEmpty() && !lista.isEmpty()) {
                perfil.nomes.add(new PerfilHistorico.NomeHistorico(perfil.nomeMinecraft, "conta", FormatoDataHistorico.formatar(lista.get(0).loginMs())));
            }
            perfis.put(uuid, perfil);
            arquivos.put(uuid, arquivo);
            return uuid;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void migrarPastaAntiga(MinecraftServer server) throws IOException {
        Path antiga = server.getWorldPath(LevelResource.ROOT).resolve("aelysium_historico");
        Path unificada = pastaBase.resolve("historico");

        for (Path pasta : List.of(antiga, unificada, unificada.resolve("ops"))) {
            if (!Files.isDirectory(pasta)) continue;
            List<Path> jsons;
            try (var stream = Files.list(pasta)) {
                jsons = stream.filter(p -> p.getFileName().toString().endsWith(".json")).toList();
            }
            for (Path json : jsons) {
                UUID uuid = carregarArquivo(json);
                if (uuid != null) salvarJogador(uuid);
                Files.deleteIfExists(json);
            }
        }

        moverPasta(antiga.resolve("anotacoes"), pastaBase.resolve("anotacoes"));
        moverPasta(antiga.resolve("exportacoes"), pastaData.resolve("exportacoes"));
        moverPasta(unificada.resolve("exportacoes"), pastaData.resolve("exportacoes"));
        for (Path pasta : List.of(antiga, unificada.resolve("ops"), unificada)) {
            try {
                Files.deleteIfExists(pasta);
            } catch (IOException ignored) {}
        }
    }

    private static void moverPasta(Path origem, Path destino) throws IOException {
        if (!Files.isDirectory(origem)) return;
        Files.createDirectories(destino);
        try (var stream = Files.list(origem)) {
            for (Path arquivo : stream.toList()) {
                Path alvo = destino.resolve(arquivo.getFileName().toString());
                if (!Files.exists(alvo)) Files.move(arquivo, alvo);
            }
        }
        try {
            Files.deleteIfExists(origem);
        } catch (IOException ignored) {}
    }

    private static void salvarJogador(UUID uuid) {
        PerfilHistorico perfil = perfis.get(uuid);
        List<Sessao> sessoes = getSessoes(uuid);
        if (perfil == null || sessoes.isEmpty()) return;

        long tempoMinutos = Math.round(getTotalHoras(uuid) * 60.0);
        gravar(uuid, perfil, perfil.toJsonSessoes(sessoes, tempoMinutos), arquivos, pastaData, pastaOps);
        if (perfil.temDadosDeMods()) {
            gravar(uuid, perfil, perfil.toJsonMods(), arquivosMods, pastaMods, pastaModsOps);
        }
    }

    private static void gravar(UUID uuid, PerfilHistorico perfil, JsonObject json, Map<UUID, Path> mapa,
                               Path pasta, Path pastaDeOps) {
        Path destino = caminhoPara(uuid, perfil, mapa, pasta, pastaDeOps);
        try {
            Files.createDirectories(destino.getParent());
            Files.writeString(destino, GSON.toJson(json), StandardCharsets.UTF_8);
            Path anterior = mapa.put(uuid, destino);
            if (anterior != null && !anterior.equals(destino)) Files.deleteIfExists(anterior);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path caminhoPara(UUID uuid, PerfilHistorico perfil, Map<UUID, Path> mapa, Path normal, Path ops) {
        Path pasta = perfil.op ? ops : normal;
        String base = sanitizarNomeArquivo(perfil.nomeAtual());
        Path candidato = pasta.resolve(base + ".json");
        for (var e : mapa.entrySet()) {
            if (!e.getKey().equals(uuid) && e.getValue().toString().equalsIgnoreCase(candidato.toString())) {
                return pasta.resolve(base + "_" + uuid.toString().substring(0, 8) + ".json");
            }
        }
        return candidato;
    }

    private static String sanitizarNomeArquivo(String nome) {
        String limpo = nome.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        while (limpo.endsWith(".") || limpo.endsWith(" ")) {
            limpo = limpo.substring(0, limpo.length() - 1);
        }
        return limpo.isBlank() ? "desconhecido" : limpo;
    }

    public static synchronized PerfilHistorico getPerfil(UUID uuid) {
        return perfis.get(uuid);
    }

    public static Path getPastaData() {
        return pastaData;
    }

    public static Path getPastaAnotacoes() {
        return pastaBase == null ? null : pastaBase.resolve("anotacoes");
    }
}
