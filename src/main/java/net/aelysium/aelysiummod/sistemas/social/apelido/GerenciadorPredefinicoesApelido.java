package net.aelysium.aelysiummod.sistemas.social.apelido;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class GerenciadorPredefinicoesApelido {

    public static final int MAX_NOME = 24;

    private GerenciadorPredefinicoesApelido() {}

    private static Path pasta(MinecraftServer servidor) {
        Path raiz = servidor.getWorldPath(LevelResource.ROOT);
        Path nova = raiz.resolve("aelysium").resolve("apelidos_predefinicoes");
        net.aelysium.aelysiummod.nucleo.armazenamento.MigracaoPasta.moverPasta(raiz.resolve("data/aelysium/predefinicoes_nick"), nova);
        return nova;
    }

    public static String normalizarNome(String bruto) {
        if (bruto == null) return "";
        String nome = bruto.trim().replaceAll("\\s+", " ");
        if (nome.length() > MAX_NOME) nome = nome.substring(0, MAX_NOME).trim();
        return nome;
    }

    private static String arquivoDe(String nome) {
        String slug = nome.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return slug.isEmpty() ? null : slug + ".json";
    }

    public static synchronized List<PredefinicaoApelido> listar(MinecraftServer servidor) {
        List<PredefinicaoApelido> lista = new ArrayList<>();
        Path pasta = pasta(servidor);
        if (!Files.isDirectory(pasta)) return lista;
        try (Stream<Path> arquivos = Files.list(pasta)) {
            for (Path arquivo : arquivos.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList()) {
                try {
                    JsonObject o = JsonParser.parseString(Files.readString(arquivo, StandardCharsets.UTF_8)).getAsJsonObject();
                    lista.add(new PredefinicaoApelido(
                            o.get("nome").getAsString(),
                            o.get("prefixo").getAsString(), o.get("prefixoCor1").getAsInt(), o.get("prefixoCor2").getAsInt(), o.get("prefixoFormato").getAsInt(),
                            o.has("nick") ? o.get("nick").getAsString() : "", o.get("nickCor").getAsInt(), o.get("nickFormato").getAsInt(),
                            o.get("sufixo").getAsString(), o.get("sufixoCor1").getAsInt(), o.get("sufixoCor2").getAsInt(), o.get("sufixoFormato").getAsInt(),
                            o.get("pronomes").getAsString(), o.get("badge").getAsString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static synchronized boolean salvar(MinecraftServer servidor, PredefinicaoApelido p) {
        String nome = normalizarNome(p.nome());
        String arquivo = arquivoDe(nome);
        if (arquivo == null) return false;
        JsonObject o = new JsonObject();
        o.addProperty("nome", nome);
        o.addProperty("prefixo", p.prefixo());
        o.addProperty("prefixoCor1", p.prefixoCor1());
        o.addProperty("prefixoCor2", p.prefixoCor2());
        o.addProperty("prefixoFormato", p.prefixoFormato());
        o.addProperty("nick", p.nick());
        o.addProperty("nickCor", p.nickCor());
        o.addProperty("nickFormato", p.nickFormato());
        o.addProperty("sufixo", p.sufixo());
        o.addProperty("sufixoCor1", p.sufixoCor1());
        o.addProperty("sufixoCor2", p.sufixoCor2());
        o.addProperty("sufixoFormato", p.sufixoFormato());
        o.addProperty("pronomes", p.pronomes());
        o.addProperty("badge", p.badge());
        try {
            Path pasta = pasta(servidor);
            Files.createDirectories(pasta);
            Files.writeString(pasta.resolve(arquivo), o.toString(), StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static synchronized boolean excluir(MinecraftServer servidor, String nome) {
        String arquivo = arquivoDe(normalizarNome(nome));
        if (arquivo == null) return false;
        try {
            return Files.deleteIfExists(pasta(servidor).resolve(arquivo));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
