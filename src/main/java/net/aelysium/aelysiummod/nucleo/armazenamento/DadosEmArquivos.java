package net.aelysium.aelysiummod.nucleo.armazenamento;

import com.google.gson.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public abstract class DadosEmArquivos extends SavedData {

    public interface Formato {
        JsonElement paraArquivo(JsonElement bruto);

        JsonElement doArquivo(JsonElement legivel);
    }

    public record Destino(String pasta, String arquivoGlobal, Map<String, String> secoes, Map<String, Formato> formatos) {
        public Destino comFormato(String secaoNbt, Formato formato) {
            Map<String, Formato> novos = new HashMap<>(formatos);
            novos.put(secaoNbt, formato);
            return new Destino(pasta, arquivoGlobal, secoes, novos);
        }

        public static Destino porJogador(String pasta, String... secoes) {
            Map<String, String> mapa = new LinkedHashMap<>();
            for (String s : secoes) {
                int igual = s.indexOf('=');
                if (igual < 0) mapa.put(s, s);
                else mapa.put(s.substring(0, igual), s.substring(igual + 1));
            }
            return new Destino(pasta, null, mapa, Map.of());
        }

        public static Destino global(String pasta, String arquivo) {
            return new Destino(pasta, arquivo, Map.of(), Map.of());
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("AelysiumArmazenamento");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, List<String>> PASTAS_ANTIGAS = Map.of(
            "perfis_sociais", List.of("deuses", "racas", "vidas", "apelidos", "insignias_jogadores"),
            "itens_banidos/jogadores", List.of("banlist_jogadores", "banlist/jogadores"));
    private static final Map<MinecraftServer, Set<String>> INICIALIZADOS = new WeakHashMap<>();
    private static final List<WeakReference<DadosEmArquivos>> VIVOS = new ArrayList<>();

    private MinecraftServer servidorDono;
    private Destino destino;
    private PastaJogadores pastaJogadores;
    private String ultimoGlobal;

    protected static synchronized <T extends DadosEmArquivos> T obter(MinecraftServer server, Factory<T> factory,
                                                                      String nomeDados, Destino destino) {
        var armazenamento = server.overworld().getDataStorage();
        Set<String> feitos = INICIALIZADOS.computeIfAbsent(server, s -> new HashSet<>());
        if (feitos.contains(nomeDados)) return armazenamento.computeIfAbsent(factory, nomeDados);
        feitos.add(nomeDados);

        HolderLookup.Provider provider = server.registryAccess();
        Path antigo = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(nomeDados + ".dat");
        T dados;
        if (Files.exists(antigo)) {
            dados = armazenamento.computeIfAbsent(factory, nomeDados);
            dados.configurar(server, destino);
            dados.setDirty();
            boolean gravou = dados.gravarTudo(provider);
            dados.setDirty(false);
            if (gravou) {
                try {
                    Files.deleteIfExists(antigo);
                    Files.deleteIfExists(antigo.resolveSibling(antigo.getFileName() + "_old"));
                } catch (IOException e) {
                    LOGGER.error("Erro ao apagar {}: ", antigo, e);
                }
            } else {
                LOGGER.error("{} nao foi convertido por completo; o .dat antigo foi mantido.", nomeDados);
            }
        } else {
            PastaJogadores pasta = destino.arquivoGlobal() == null ? PastaJogadores.de(pastaDe(server, destino)) : null;
            if (pasta != null) {
                pasta.recarregar();
                for (String antiga : PASTAS_ANTIGAS.getOrDefault(destino.pasta(), List.of())) {
                    pasta.importarDe(server.getWorldPath(LevelResource.ROOT).resolve("aelysium").resolve(antiga), server);
                }
            }
            CompoundTag tag = destino.arquivoGlobal() == null ? montarTag(pasta, destino) : lerGlobal(server, destino);
            dados = factory.deserializer().apply(tag, provider);
            armazenamento.set(nomeDados, dados);
            dados.configurar(server, destino);
        }
        return dados;
    }

    void configurar(MinecraftServer server, Destino destino) {
        this.servidorDono = server;
        this.destino = destino;
        if (destino.arquivoGlobal() == null) {
            this.pastaJogadores = PastaJogadores.de(pastaDe(server, destino));
            this.pastaJogadores.recarregarSeVazia();
        }
        VIVOS.add(new WeakReference<>(this));
    }

    private static Path pastaDe(MinecraftServer server, Destino destino) {
        return server.getWorldPath(LevelResource.ROOT).resolve("aelysium").resolve(destino.pasta());
    }

    private static CompoundTag lerGlobal(MinecraftServer server, Destino destino) {
        Path arquivo = pastaDe(server, destino).resolve(destino.arquivoGlobal());
        if (Files.exists(arquivo)) {
            try {
                JsonObject root = JsonParser.parseString(Files.readString(arquivo, StandardCharsets.UTF_8)).getAsJsonObject();
                return (CompoundTag) ConversorNbtJson.deJson(root);
            } catch (Exception e) {
                LOGGER.error("Erro ao ler {}: ", arquivo, e);
            }
        }
        return new CompoundTag();
    }

    private static CompoundTag montarTag(PastaJogadores pasta, Destino destino) {
        CompoundTag tag = new CompoundTag();
        for (var secao : destino.secoes().entrySet()) {
            CompoundTag conteudo = new CompoundTag();
            for (UUID uuid : pasta.jogadores()) {
                JsonElement valor = pasta.secao(uuid, secao.getValue());
                Formato formato = destino.formatos().get(secao.getKey());
                if (valor != null && formato != null) valor = formato.doArquivo(valor);
                if (valor != null) conteudo.put(uuid.toString(), ConversorNbtJson.deJson(valor));
            }
            tag.put(secao.getKey(), conteudo);
        }
        return tag;
    }

    @Override
    public void save(File file, HolderLookup.Provider provider) {
        if (destino == null || !isDirty()) {
            super.save(file, provider);
            return;
        }
        gravarTudo(provider);
        setDirty(false);
    }

    boolean gravarTudo(HolderLookup.Provider provider) {
        CompoundTag tag = save(new CompoundTag(), provider);
        if (destino.arquivoGlobal() != null) {
            return gravarGlobal(tag);
        }
        boolean ok = true;

        synchronized (pastaJogadores) {
            Map<UUID, CompoundTag> fatias = dividir(tag);
            Set<UUID> alterados = new LinkedHashSet<>();
            for (var entrada : fatias.entrySet()) {
                aplicarFatia(entrada.getKey(), entrada.getValue());
                alterados.add(entrada.getKey());
            }
            for (UUID uuid : pastaJogadores.jogadores()) {
                if (fatias.containsKey(uuid)) continue;
                boolean mudou = false;
                for (String chave : destino.secoes().values()) mudou |= pastaJogadores.definir(uuid, chave, null);
                if (mudou) alterados.add(uuid);
            }
            for (UUID uuid : alterados) ok &= pastaJogadores.gravar(uuid, servidorDono);
        }
        return ok;
    }

    private boolean gravarGlobal(CompoundTag tag) {
        try {
            Path arquivo = pastaDe(servidorDono, destino).resolve(destino.arquivoGlobal());
            String texto = GSON.toJson(ConversorNbtJson.paraJson(tag));
            if (texto.equals(ultimoGlobal) && Files.exists(arquivo)) return true;
            Files.createDirectories(arquivo.getParent());
            Files.writeString(arquivo, texto, StandardCharsets.UTF_8);
            ultimoGlobal = texto;
            return true;
        } catch (IOException e) {
            LOGGER.error("Erro ao gravar dados em {}: ", destino.pasta(), e);
            return false;
        }
    }

    private Map<UUID, CompoundTag> dividir(CompoundTag tag) {
        Map<UUID, CompoundTag> fatias = new LinkedHashMap<>();
        for (String secao : destino.secoes().keySet()) {
            if (!tag.contains(secao, Tag.TAG_COMPOUND)) continue;
            CompoundTag conteudo = tag.getCompound(secao);
            for (String chave : conteudo.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(chave);
                    fatias.computeIfAbsent(uuid, u -> new CompoundTag()).put(secao, conteudo.get(chave));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return fatias;
    }

    private boolean aplicarFatia(UUID uuid, CompoundTag fatia) {
        boolean mudou = false;
        for (var secao : destino.secoes().entrySet()) {
            Tag valor = fatia.get(secao.getKey());
            JsonElement json = valor == null ? null : ConversorNbtJson.paraJson(valor);
            Formato formato = destino.formatos().get(secao.getKey());
            if (json != null && formato != null) json = formato.paraArquivo(json);
            mudou |= pastaJogadores.definir(uuid, secao.getValue(), json);
        }
        return mudou;
    }

    private void regravarJogador(UUID uuid) {
        if (destino == null || destino.arquivoGlobal() != null || servidorDono == null) return;
        synchronized (pastaJogadores) {
            CompoundTag fatia = dividir(save(new CompoundTag(), servidorDono.registryAccess())).get(uuid);
            if (fatia != null) aplicarFatia(uuid, fatia);
            pastaJogadores.gravar(uuid, servidorDono);
        }
    }

    public static synchronized void aoJogadorMudou(UUID uuid) {
        VIVOS.removeIf(ref -> ref.get() == null);
        for (WeakReference<DadosEmArquivos> ref : new ArrayList<>(VIVOS)) {
            DadosEmArquivos dados = ref.get();
            if (dados != null) dados.regravarJogador(uuid);
        }
    }
}
