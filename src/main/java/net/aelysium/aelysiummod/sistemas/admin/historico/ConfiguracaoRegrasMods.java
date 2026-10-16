package net.aelysium.aelysiummod.sistemas.admin.historico;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.gson.*;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.configuracao.AelysiumDiretorioConfiguracao;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfiguracaoRegrasMods {

    public static final String APROVADO = "aprovado";
    public static final String PROIBIDO = "proibido";
    public static final String DESCONHECIDO = "desconhecido";

    private static final String MENSAGEM_PADRAO = "§cVocê está usando o mod proibido: §f{mod}\n§7Remova-o e tente entrar novamente.";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path arquivo;
    private static Set<String> modpack = new TreeSet<>();
    private static Map<String, String> regras = new TreeMap<>();
    private static Map<String, Set<String>> hashesConfiaveis = new TreeMap<>();
    private static boolean bloqueioAtivo = true;
    private static String mensagemDesconexao = MENSAGEM_PADRAO;
    private static boolean logarBloqueios = true;
    private static boolean mensagensEntradaSaida = true;
    private static boolean manutencaoAtiva = false;
    private static JsonObject spawnMundo = null;
    private static final Map<String, String> hashesServidor = new ConcurrentHashMap<>();

    public static synchronized void inicializar(MinecraftServer server) {
        arquivo = server.getWorldPath(LevelResource.ROOT).resolve("aelysium").resolve("regras_mods.json");
        boolean existia = Files.exists(arquivo);
        if (existia) {
            carregar();
        } else {
            modpack = new TreeSet<>();
            for (ModCliente mod : UtilitariosMods.coletar(false)) modpack.add(mod.id().toLowerCase());
            regras = new TreeMap<>();
            hashesConfiaveis = new TreeMap<>();
        }
        boolean migrouLista = migrarListaNegraAntiga();
        boolean migrou = migrouLista | migrarModToml(existia);
        if (!existia || migrou) salvar();
        if (migrouLista && Files.exists(arquivo)) {
            try {
                Files.deleteIfExists(AelysiumDiretorioConfiguracao.file("mods_banidos.toml"));
                LoggerFactory.getLogger(AelysiumMod.MOD_ID).info("mods_banidos.toml convertido para regras_mods.json e apagado.");
            } catch (IOException e) {
                LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao apagar mods_banidos.toml: ", e);
            }
        }

        Thread calculo = new Thread(() -> {
            for (ModCliente mod : UtilitariosMods.coletar(true)) {
                if (!mod.sha1().isEmpty()) hashesServidor.put(mod.id().toLowerCase(), mod.sha1());
            }
        }, "aelysium-hash-mods-servidor");
        calculo.setDaemon(true);
        calculo.start();
    }

    public static synchronized void recarregar() {
        if (arquivo != null && Files.exists(arquivo)) carregar();
    }

    public static synchronized Set<String> modpackServidor() {
        return new TreeSet<>(modpack);
    }

    public static synchronized String status(String modId) {
        String valor = regras.get(modId.toLowerCase());
        if (valor == null) return DESCONHECIDO;
        String limpo = valor.toLowerCase();
        return limpo.equals(APROVADO) || limpo.equals(PROIBIDO) ? limpo : DESCONHECIDO;
    }

    public static synchronized String checarBloqueio(List<String> idsCliente) {
        if (!bloqueioAtivo || idsCliente == null) return null;
        for (String id : idsCliente) {
            if (id != null && PROIBIDO.equals(status(id))) return id.toLowerCase().trim();
        }
        return null;
    }

    public static synchronized String mensagemDesconexao(String mod) {
        return mensagemDesconexao.replace("{mod}", mod);
    }

    public static synchronized boolean logarBloqueios() {
        return logarBloqueios;
    }

    public static synchronized JsonObject spawnMundo() {
        return spawnMundo == null ? null : spawnMundo.deepCopy();
    }

    public static synchronized void definirSpawnMundo(JsonObject spawn) {
        spawnMundo = spawn == null ? null : spawn.deepCopy();
        if (arquivo != null) salvar();
    }

    public static synchronized boolean manutencaoAtiva() {
        return manutencaoAtiva;
    }

    public static synchronized void definirManutencao(boolean ativa) {
        manutencaoAtiva = ativa;
        if (arquivo != null) salvar();
    }

    public static synchronized boolean mensagensEntradaSaida() {
        return mensagensEntradaSaida;
    }

    public static synchronized void definirMensagensEntradaSaida(boolean valor) {
        mensagensEntradaSaida = valor;
        if (arquivo != null) salvar();
    }

    private static boolean migrarModToml(boolean jaTinhaArquivo) {
        Path raiz = arquivo.getParent();
        boolean migrou = false;
        try {
            Path toml = raiz.resolve("mod.toml");
            if (Files.exists(toml)) {
                CommentedFileConfig cfg = CommentedFileConfig.builder(toml).charset(StandardCharsets.UTF_8).build();
                cfg.load();
                if (!jaTinhaArquivo) mensagensEntradaSaida = cfg.getOrElse("mensagens_entrada_saida", true);
                cfg.close();
                Files.deleteIfExists(toml);
                migrou = true;
            }
            for (Path antigo : new Path[]{raiz.resolve("mod.properties"), AelysiumDiretorioConfiguracao.file("mod.properties")}) {
                if (!Files.exists(antigo)) continue;
                java.util.Properties props = new java.util.Properties();
                try (java.io.InputStream in = Files.newInputStream(antigo)) {
                    props.load(in);
                }
                if (!jaTinhaArquivo) mensagensEntradaSaida = Boolean.parseBoolean(props.getProperty("joinLeaveMessagesEnabled", "true"));
                Files.deleteIfExists(antigo);
                migrou = true;
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao migrar o mod.toml: ", e);
        }
        return migrou;
    }

    public static synchronized boolean alternarProibido(String modId) {
        String id = modId.toLowerCase();
        boolean removeu = PROIBIDO.equals(status(id));
        if (removeu) {
            regras.remove(id);
        } else {
            regras.put(id, PROIBIDO);
            bloqueioAtivo = true;
        }
        if (arquivo != null) salvar();
        return removeu;
    }

    public static synchronized Boolean hashConfiavel(String modId, String sha1) {
        if (sha1 == null || sha1.isEmpty()) return null;
        String id = modId.toLowerCase();
        Set<String> confiaveis = new HashSet<>(hashesConfiaveis.getOrDefault(id, Set.of()));
        String doServidor = hashesServidor.get(id);
        if (doServidor != null) confiaveis.add(doServidor);
        if (confiaveis.isEmpty()) return null;
        return confiaveis.contains(sha1.toLowerCase());
    }

    private static boolean migrarListaNegraAntiga() {
        Path toml = AelysiumDiretorioConfiguracao.file("mods_banidos.toml");
        if (!Files.exists(toml)) return false;
        try {
            CommentedFileConfig cfg = CommentedFileConfig.builder(toml).charset(StandardCharsets.UTF_8).build();
            cfg.load();
            boolean ativoAntigo = cfg.getOrElse("ativo", false);
            List<String> bloqueados = cfg.getOrElse("mods_bloqueados", new ArrayList<>());
            String mensagem = cfg.getOrElse("mensagem_desconexao", MENSAGEM_PADRAO);
            boolean logar = cfg.getOrElse("logar_bloqueios", true);
            cfg.close();

            for (String mod : bloqueados) {
                if (mod != null && !mod.isBlank()) regras.put(mod.toLowerCase().trim(), PROIBIDO);
            }
            if (!bloqueados.isEmpty()) bloqueioAtivo = ativoAntigo;
            mensagemDesconexao = mensagem;
            logarBloqueios = logar;

            LoggerFactory.getLogger(AelysiumMod.MOD_ID).info("mods_banidos.toml lido ({} mods proibidos).", bloqueados.size());
            return true;
        } catch (Exception e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao migrar mods_banidos.toml: ", e);
            return false;
        }
    }

    private static void carregar() {
        try {
            JsonObject root = JsonParser.parseString(Files.readString(arquivo, StandardCharsets.UTF_8)).getAsJsonObject();
            Set<String> novoModpack = new TreeSet<>();
            if (root.has("modpackServidor")) {
                for (JsonElement el : root.getAsJsonArray("modpackServidor")) novoModpack.add(el.getAsString().toLowerCase());
            }
            Map<String, String> novasRegras = new TreeMap<>();
            if (root.has("regras")) {
                for (var e : root.getAsJsonObject("regras").entrySet()) novasRegras.put(e.getKey().toLowerCase(), e.getValue().getAsString());
            }
            Map<String, Set<String>> novosHashes = new TreeMap<>();
            if (root.has("hashesConfiaveis")) {
                for (var e : root.getAsJsonObject("hashesConfiaveis").entrySet()) {
                    Set<String> hashes = new TreeSet<>();
                    for (JsonElement el : e.getValue().getAsJsonArray()) hashes.add(el.getAsString().toLowerCase());
                    novosHashes.put(e.getKey().toLowerCase(), hashes);
                }
            }
            boolean ativo = true;
            String mensagem = MENSAGEM_PADRAO;
            boolean logar = true;
            mensagensEntradaSaida = !root.has("mensagensEntradaSaida") || root.get("mensagensEntradaSaida").getAsBoolean();
            manutencaoAtiva = root.has("manutencaoAtiva") && root.get("manutencaoAtiva").getAsBoolean();
            if (root.has("bloqueio")) {
                JsonObject bloqueio = root.getAsJsonObject("bloqueio");
                if (bloqueio.has("ativo")) ativo = bloqueio.get("ativo").getAsBoolean();
                if (bloqueio.has("mensagemDesconexao")) mensagem = bloqueio.get("mensagemDesconexao").getAsString();
                if (bloqueio.has("logarBloqueios")) logar = bloqueio.get("logarBloqueios").getAsBoolean();
            }
            modpack = novoModpack;
            regras = novasRegras;
            hashesConfiaveis = novosHashes;
            spawnMundo = root.has("spawnMundo") && root.get("spawnMundo").isJsonObject() ? root.getAsJsonObject("spawnMundo") : null;
            bloqueioAtivo = ativo;
            mensagemDesconexao = mensagem;
            logarBloqueios = logar;
        } catch (Exception e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao ler regras_mods.json: ", e);
        }
    }

    private static void salvar() {
        JsonObject root = new JsonObject();
        JsonArray pack = new JsonArray();
        for (String id : modpack) pack.add(id);
        root.add("modpackServidor", pack);
        root.addProperty("mensagensEntradaSaida", mensagensEntradaSaida);
        root.addProperty("manutencaoAtiva", manutencaoAtiva);

        JsonObject bloqueio = new JsonObject();
        bloqueio.addProperty("ativo", bloqueioAtivo);
        bloqueio.addProperty("mensagemDesconexao", mensagemDesconexao);
        bloqueio.addProperty("logarBloqueios", logarBloqueios);
        root.add("bloqueio", bloqueio);

        JsonObject regrasJson = new JsonObject();
        for (var e : regras.entrySet()) regrasJson.addProperty(e.getKey(), e.getValue());
        root.add("regras", regrasJson);

        JsonObject hashesJson = new JsonObject();
        for (var e : hashesConfiaveis.entrySet()) {
            JsonArray lista = new JsonArray();
            for (String h : e.getValue()) lista.add(h);
            hashesJson.add(e.getKey(), lista);
        }
        root.add("hashesConfiaveis", hashesJson);
        if (spawnMundo != null) root.add("spawnMundo", spawnMundo);

        try {
            Files.createDirectories(arquivo.getParent());
            Files.writeString(arquivo, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao salvar regras_mods.json: ", e);
        }
    }
}
