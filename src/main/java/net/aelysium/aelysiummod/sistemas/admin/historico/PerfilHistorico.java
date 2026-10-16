package net.aelysium.aelysiummod.sistemas.admin.historico;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

public class PerfilHistorico {

    public record NomeHistorico(String nome, String tipo, String desde) {}

    public record OpHistorico(boolean op, String em, String por) {}

    public record Evento(String em, List<String> adicionados, List<String> removidos, List<String> hashAlterado) {}

    public static class ModRegistrado {
        public String versao = "";
        public String arquivo = "";
        public String sha1 = "";
        public String status = ConfiguracaoRegrasMods.DESCONHECIDO;
        public String primeiraVez = "";
        public String ultimaVez = "";
        public int sessoes;
        public boolean hashSuspeito;
    }

    private static final int MAX_EVENTOS = 500;

    public final UUID uuid;
    public String nomeMinecraft = "";
    public String apelido;
    public final List<NomeHistorico> nomes = new ArrayList<>();

    public boolean op;
    public final List<OpHistorico> opHistorico = new ArrayList<>();

    public String minecraft = "";
    public String loader = "";
    public String loaderVersao = "";
    public boolean connector;

    public List<String> modsFaltando = new ArrayList<>();
    public Map<String, ModRegistrado> modsExtras = new LinkedHashMap<>();
    public Map<String, ModRegistrado> modsPackAlterados = new LinkedHashMap<>();
    public final List<Evento> eventos = new ArrayList<>();

    public PerfilHistorico(UUID uuid) {
        this.uuid = uuid;
    }

    public String nomeAtual() {
        if (apelido != null && !apelido.isBlank()) return apelido;
        return nomeMinecraft.isBlank() ? uuid.toString().substring(0, 8) : nomeMinecraft;
    }

    public void atualizarNomes(String nomeConta, String novoApelido, String agora) {
        String apelidoLimpo = novoApelido == null || novoApelido.isBlank() ? null : novoApelido;

        if (nomeConta != null && !nomeConta.isBlank() && !nomeConta.equals(ultimoNome("conta"))) {
            nomes.add(new NomeHistorico(nomeConta, "conta", agora));
        }
        String ultimoApelido = ultimoNome("apelido");
        if (apelidoLimpo != null) {
            if (!apelidoLimpo.equals(ultimoApelido)) nomes.add(new NomeHistorico(apelidoLimpo, "apelido", agora));
        } else if (ultimoApelido != null && !"sem_apelido".equals(ultimoTipoDeApelido())) {
            nomes.add(new NomeHistorico(nomeConta == null ? "" : nomeConta, "sem_apelido", agora));
        }

        if (nomeConta != null && !nomeConta.isBlank()) nomeMinecraft = nomeConta;
        apelido = apelidoLimpo;
    }

    private String ultimoNome(String tipo) {
        for (int i = nomes.size() - 1; i >= 0; i--) {
            if (nomes.get(i).tipo().equals(tipo)) return nomes.get(i).nome();
        }
        return null;
    }

    private String ultimoTipoDeApelido() {
        for (int i = nomes.size() - 1; i >= 0; i--) {
            String tipo = nomes.get(i).tipo();
            if (tipo.equals("apelido") || tipo.equals("sem_apelido")) return tipo;
        }
        return null;
    }

    public void registrarOp(boolean novoOp, String por, String agora) {
        op = novoOp;
        opHistorico.add(new OpHistorico(novoOp, agora, por));
    }

    public void atualizarMods(String minecraftCliente, String loaderCliente, String loaderVersaoCliente,
                              List<ModCliente> recebidos, String agora) {
        minecraft = minecraftCliente;
        loader = loaderCliente;
        loaderVersao = loaderVersaoCliente;

        Map<String, ModCliente> cliente = new TreeMap<>();
        for (ModCliente mod : recebidos) cliente.put(mod.id(), mod);

        connector = cliente.containsKey("connector") || cliente.keySet().stream().anyMatch(id -> id.startsWith("fabric_"));

        Set<String> pack = ConfiguracaoRegrasMods.modpackServidor();

        Map<String, ModRegistrado> novosExtras = new LinkedHashMap<>();
        for (var entrada : cliente.entrySet()) {
            String id = entrada.getKey();
            if (pack.contains(id)) continue;
            ModCliente mod = entrada.getValue();
            ModRegistrado anterior = modsExtras.get(id);

            ModRegistrado registro = new ModRegistrado();
            registro.versao = mod.versao();
            registro.arquivo = mod.arquivo();
            registro.sha1 = mod.sha1();
            registro.status = ConfiguracaoRegrasMods.status(id);
            registro.primeiraVez = anterior != null ? anterior.primeiraVez : agora;
            registro.ultimaVez = agora;
            registro.sessoes = anterior != null ? anterior.sessoes + 1 : 1;
            registro.hashSuspeito = Boolean.FALSE.equals(ConfiguracaoRegrasMods.hashConfiavel(id, mod.sha1()));
            novosExtras.put(id, registro);
        }

        Map<String, ModRegistrado> novosAlterados = new LinkedHashMap<>();
        for (String id : pack) {
            ModCliente mod = cliente.get(id);
            if (mod == null) continue;
            if (!Boolean.FALSE.equals(ConfiguracaoRegrasMods.hashConfiavel(id, mod.sha1()))) continue;
            ModRegistrado anterior = modsPackAlterados.get(id);

            ModRegistrado registro = new ModRegistrado();
            registro.versao = mod.versao();
            registro.arquivo = mod.arquivo();
            registro.sha1 = mod.sha1();
            registro.status = ConfiguracaoRegrasMods.status(id);
            registro.primeiraVez = anterior != null ? anterior.primeiraVez : agora;
            registro.ultimaVez = agora;
            registro.sessoes = anterior != null ? anterior.sessoes + 1 : 1;
            registro.hashSuspeito = true;
            novosAlterados.put(id, registro);
        }

        List<String> novoFaltando = new ArrayList<>();
        for (String id : pack) if (!cliente.containsKey(id)) novoFaltando.add(id);

        Set<String> adicionados = new TreeSet<>();
        Set<String> removidos = new TreeSet<>();
        Set<String> hashAlterado = new TreeSet<>();

        for (var e : novosExtras.entrySet()) {
            ModRegistrado anterior = modsExtras.get(e.getKey());
            if (anterior == null) adicionados.add(e.getKey());
            else if (mudou(anterior, e.getValue())) hashAlterado.add(e.getKey());
        }
        for (String id : modsExtras.keySet()) {
            if (!novosExtras.containsKey(id) && !cliente.containsKey(id)) removidos.add(id);
        }
        for (String id : novoFaltando) {
            if (!modsFaltando.contains(id)) removidos.add(id);
        }
        for (String id : modsFaltando) {
            if (cliente.containsKey(id)) adicionados.add(id);
        }
        for (var e : novosAlterados.entrySet()) {
            ModRegistrado anterior = modsPackAlterados.get(e.getKey());
            if (anterior == null || mudou(anterior, e.getValue())) hashAlterado.add(e.getKey());
        }
        for (String id : modsPackAlterados.keySet()) {
            if (!novosAlterados.containsKey(id) && cliente.containsKey(id)) hashAlterado.add(id);
        }

        modsExtras = novosExtras;
        modsPackAlterados = novosAlterados;
        modsFaltando = novoFaltando;

        if (!adicionados.isEmpty() || !removidos.isEmpty() || !hashAlterado.isEmpty()) {
            eventos.add(new Evento(agora, new ArrayList<>(adicionados), new ArrayList<>(removidos), new ArrayList<>(hashAlterado)));
            while (eventos.size() > MAX_EVENTOS) eventos.remove(0);
        }
    }

    private static boolean mudou(ModRegistrado antes, ModRegistrado depois) {
        return !Objects.equals(antes.sha1, depois.sha1) || !Objects.equals(antes.versao, depois.versao);
    }

    public JsonObject cabecalho() {
        JsonObject root = new JsonObject();
        root.addProperty("uuid", uuid.toString());
        root.addProperty("nome", nomeAtual());
        root.addProperty("nomeMinecraft", nomeMinecraft);

        JsonArray nomesJson = new JsonArray();
        for (NomeHistorico n : nomes) {
            JsonObject obj = new JsonObject();
            obj.addProperty("nome", n.nome());
            obj.addProperty("tipo", n.tipo());
            obj.addProperty("desde", n.desde());
            nomesJson.add(obj);
        }
        root.add("historicoNomes", nomesJson);
        return root;
    }

    public boolean temDadosDeMods() {
        return !minecraft.isBlank() || !eventos.isEmpty() || !modsExtras.isEmpty() || !modsFaltando.isEmpty();
    }

    public JsonObject toJsonSessoes(List<DadosHistorico.Sessao> sessoes, long tempoJogadoMinutos) {
        JsonObject root = cabecalho();

        JsonObject opJson = new JsonObject();
        opJson.addProperty("atual", op);
        JsonArray opHist = new JsonArray();
        for (OpHistorico h : opHistorico) {
            JsonObject obj = new JsonObject();
            obj.addProperty("op", h.op());
            obj.addProperty("em", h.em());
            obj.addProperty("por", h.por());
            opHist.add(obj);
        }
        opJson.add("historico", opHist);
        root.add("op", opJson);

        if (!sessoes.isEmpty()) {
            root.addProperty("primeiraConexao", FormatoDataHistorico.formatar(sessoes.get(0).loginMs()));
            root.addProperty("ultimaConexao", FormatoDataHistorico.formatar(sessoes.get(sessoes.size() - 1).loginMs()));
        }
        root.addProperty("totalSessoes", sessoes.size());
        root.addProperty("tempoJogadoMinutos", tempoJogadoMinutos);

        JsonArray sessoesJson = new JsonArray();
        for (DadosHistorico.Sessao s : sessoes) {
            JsonObject obj = new JsonObject();
            obj.addProperty("nome", s.nomeJogador());
            obj.addProperty("login", FormatoDataHistorico.formatar(s.loginMs()));
            obj.addProperty("logout", s.finalizada() ? FormatoDataHistorico.formatar(s.logoutMs()) : "");
            obj.addProperty("ip", s.ip());
            sessoesJson.add(obj);
        }
        root.add("sessoes", sessoesJson);
        return root;
    }

    public JsonObject toJsonMods() {
        JsonObject root = cabecalho();

        JsonObject clienteJson = new JsonObject();
        clienteJson.addProperty("minecraft", minecraft);
        clienteJson.addProperty("loader", loader);
        clienteJson.addProperty("loaderVersao", loaderVersao);
        clienteJson.addProperty("connector", connector);
        root.add("cliente", clienteJson);

        JsonArray faltando = new JsonArray();
        for (String id : modsFaltando) faltando.add(id);
        root.add("modsFaltando", faltando);
        root.add("modsExtras", modsParaJson(modsExtras));
        if (!modsPackAlterados.isEmpty()) root.add("modsPackAlterados", modsParaJson(modsPackAlterados));

        JsonArray eventosJson = new JsonArray();
        for (Evento ev : eventos) {
            JsonObject obj = new JsonObject();
            obj.addProperty("em", ev.em());
            obj.add("adicionados", listaParaJson(ev.adicionados()));
            obj.add("removidos", listaParaJson(ev.removidos()));
            obj.add("hashAlterado", listaParaJson(ev.hashAlterado()));
            eventosJson.add(obj);
        }
        root.add("eventos", eventosJson);
        return root;
    }

    public void copiarMods(PerfilHistorico outro) {
        minecraft = outro.minecraft;
        loader = outro.loader;
        loaderVersao = outro.loaderVersao;
        connector = outro.connector;
        modsFaltando = outro.modsFaltando;
        modsExtras = outro.modsExtras;
        modsPackAlterados = outro.modsPackAlterados;
        eventos.clear();
        eventos.addAll(outro.eventos);
    }

    public static PerfilHistorico deJson(UUID uuid, JsonObject root) {
        PerfilHistorico perfil = new PerfilHistorico(uuid);
        perfil.nomeMinecraft = texto(root, "nomeMinecraft", texto(root, "nome", ""));

        if (root.has("historicoNomes")) {
            for (JsonElement el : root.getAsJsonArray("historicoNomes")) {
                JsonObject obj = el.getAsJsonObject();
                perfil.nomes.add(new NomeHistorico(texto(obj, "nome", ""), texto(obj, "tipo", "conta"), texto(obj, "desde", "")));
            }
        }
        for (int i = perfil.nomes.size() - 1; i >= 0; i--) {
            if (perfil.nomes.get(i).tipo().equals("apelido")) {
                perfil.apelido = perfil.nomes.get(i).nome();
                break;
            }
            if (perfil.nomes.get(i).tipo().equals("sem_apelido")) break;
        }

        if (root.has("op")) {
            JsonObject opJson = root.getAsJsonObject("op");
            perfil.op = opJson.has("atual") && opJson.get("atual").getAsBoolean();
            if (opJson.has("historico")) {
                for (JsonElement el : opJson.getAsJsonArray("historico")) {
                    JsonObject obj = el.getAsJsonObject();
                    perfil.opHistorico.add(new OpHistorico(obj.get("op").getAsBoolean(), texto(obj, "em", ""), texto(obj, "por", "desconhecido")));
                }
            }
        }

        if (root.has("cliente")) {
            JsonObject c = root.getAsJsonObject("cliente");
            perfil.minecraft = texto(c, "minecraft", "");
            perfil.loader = texto(c, "loader", "");
            perfil.loaderVersao = texto(c, "loaderVersao", "");
            perfil.connector = c.has("connector") && c.get("connector").getAsBoolean();
        }

        if (root.has("modsFaltando")) {
            for (JsonElement el : root.getAsJsonArray("modsFaltando")) perfil.modsFaltando.add(el.getAsString());
        }
        if (root.has("modsExtras")) perfil.modsExtras = modsDeJson(root.getAsJsonObject("modsExtras"));
        if (root.has("modsPackAlterados")) perfil.modsPackAlterados = modsDeJson(root.getAsJsonObject("modsPackAlterados"));

        if (root.has("eventos")) {
            for (JsonElement el : root.getAsJsonArray("eventos")) {
                JsonObject obj = el.getAsJsonObject();
                perfil.eventos.add(new Evento(texto(obj, "em", ""), listaDeJson(obj, "adicionados"),
                        listaDeJson(obj, "removidos"), listaDeJson(obj, "hashAlterado")));
            }
        }
        return perfil;
    }

    private static JsonObject modsParaJson(Map<String, ModRegistrado> mods) {
        JsonObject json = new JsonObject();
        for (var e : mods.entrySet()) {
            ModRegistrado m = e.getValue();
            JsonObject obj = new JsonObject();
            obj.addProperty("versao", m.versao);
            obj.addProperty("arquivo", m.arquivo);
            obj.addProperty("sha1", m.sha1);
            obj.addProperty("status", m.status);
            if (m.hashSuspeito) obj.addProperty("hashSuspeito", true);
            obj.addProperty("primeiraVez", m.primeiraVez);
            obj.addProperty("ultimaVez", m.ultimaVez);
            obj.addProperty("sessoes", m.sessoes);
            json.add(e.getKey(), obj);
        }
        return json;
    }

    private static Map<String, ModRegistrado> modsDeJson(JsonObject json) {
        Map<String, ModRegistrado> mods = new LinkedHashMap<>();
        for (var e : json.entrySet()) {
            JsonObject obj = e.getValue().getAsJsonObject();
            ModRegistrado m = new ModRegistrado();
            m.versao = texto(obj, "versao", "");
            m.arquivo = texto(obj, "arquivo", "");
            m.sha1 = texto(obj, "sha1", "");
            m.status = texto(obj, "status", ConfiguracaoRegrasMods.DESCONHECIDO);
            m.hashSuspeito = obj.has("hashSuspeito") && obj.get("hashSuspeito").getAsBoolean();
            m.primeiraVez = texto(obj, "primeiraVez", "");
            m.ultimaVez = texto(obj, "ultimaVez", "");
            m.sessoes = obj.has("sessoes") ? obj.get("sessoes").getAsInt() : 0;
            mods.put(e.getKey(), m);
        }
        return mods;
    }

    private static JsonArray listaParaJson(List<String> lista) {
        JsonArray arr = new JsonArray();
        for (String s : lista) arr.add(s);
        return arr;
    }

    private static List<String> listaDeJson(JsonObject obj, String chave) {
        List<String> lista = new ArrayList<>();
        if (obj.has(chave)) for (JsonElement el : obj.getAsJsonArray(chave)) lista.add(el.getAsString());
        return lista;
    }

    private static String texto(JsonObject obj, String chave, String padrao) {
        return obj.has(chave) && !obj.get(chave).isJsonNull() ? obj.get(chave).getAsString() : padrao;
    }
}
