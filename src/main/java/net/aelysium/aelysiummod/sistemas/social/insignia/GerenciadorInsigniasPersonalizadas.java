package net.aelysium.aelysiummod.sistemas.social.insignia;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.aelysium.aelysiummod.nucleo.jogador.GeradorInsignia;
import net.aelysium.aelysiummod.nucleo.jogador.InsigniaPersonalizada;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class GerenciadorInsigniasPersonalizadas {

    private static final Map<String, InsigniaPersonalizada> TODAS = new LinkedHashMap<>();

    private GerenciadorInsigniasPersonalizadas() {}

    private static Path pasta(MinecraftServer servidor) {
        Path raiz = servidor.getWorldPath(LevelResource.ROOT);
        Path nova = raiz.resolve("aelysium").resolve("insignias");
        net.aelysium.aelysiummod.nucleo.armazenamento.MigracaoPasta.moverPasta(raiz.resolve("data/aelysium/insignias"), nova);
        return nova;
    }

    public static synchronized void carregar(MinecraftServer servidor) {
        TODAS.clear();
        Path pasta = pasta(servidor);
        if (!Files.isDirectory(pasta)) return;
        try (Stream<Path> arquivos = Files.list(pasta)) {
            List<Path> jsons = arquivos.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList();
            for (Path json : jsons) {
                try {
                    JsonObject o = JsonParser.parseString(Files.readString(json, StandardCharsets.UTF_8)).getAsJsonObject();
                    InsigniaPersonalizada insignia = new InsigniaPersonalizada(
                            o.get("id").getAsString(), o.get("texto").getAsString(),
                            o.get("cor").getAsInt(), o.get("indice").getAsInt());
                    if (insignia.indice() >= 0 && insignia.indice() < InsigniaPersonalizada.MAXIMO) {
                        TODAS.put(insignia.id(), insignia);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void limpar() {
        TODAS.clear();
    }

    public static synchronized Collection<InsigniaPersonalizada> todas() {
        return new ArrayList<>(TODAS.values());
    }

    public static synchronized InsigniaPersonalizada porId(String id) {
        return id == null ? null : TODAS.get(id);
    }

    public static synchronized InsigniaPersonalizada criar(MinecraftServer servidor, String textoBruto, int cor) {
        String texto = GeradorInsignia.normalizar(textoBruto);
        if (texto.isEmpty()) return null;
        int rgb = cor & 0xFFFFFF;
        String id = InsigniaPersonalizada.idPara(texto, rgb);

        InsigniaPersonalizada existente = TODAS.get(id);
        int indice;
        if (existente != null) {
            indice = existente.indice();
        } else {
            boolean[] usados = new boolean[InsigniaPersonalizada.MAXIMO];
            for (InsigniaPersonalizada i : TODAS.values()) usados[i.indice()] = true;
            indice = -1;
            for (int i = 0; i < usados.length; i++) {
                if (!usados[i]) {
                    indice = i;
                    break;
                }
            }
            if (indice < 0) return null;
        }

        InsigniaPersonalizada nova = new InsigniaPersonalizada(id, texto, rgb, indice);
        try {
            Path pasta = pasta(servidor);
            Files.createDirectories(pasta);
            JsonObject o = new JsonObject();
            o.addProperty("id", id);
            o.addProperty("texto", texto);
            o.addProperty("cor", rgb);
            o.addProperty("indice", indice);
            Files.writeString(pasta.resolve(id + ".json"), o.toString(), StandardCharsets.UTF_8);
            Files.write(pasta.resolve(id + ".png"), GeradorInsignia.png(GeradorInsignia.gerar(texto, rgb)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        TODAS.put(id, nova);
        return nova;
    }

    public static synchronized boolean excluir(MinecraftServer servidor, String id) {
        InsigniaPersonalizada removida = TODAS.remove(id);
        if (removida == null) return false;
        try {
            Path pasta = pasta(servidor);
            Files.deleteIfExists(pasta.resolve(id + ".json"));
            Files.deleteIfExists(pasta.resolve(id + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
