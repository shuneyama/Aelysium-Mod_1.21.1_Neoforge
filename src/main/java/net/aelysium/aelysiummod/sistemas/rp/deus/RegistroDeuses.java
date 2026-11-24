package net.aelysium.aelysiummod.sistemas.rp.deus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class RegistroDeuses {
    public static final int TAMANHO_MAXIMO_NOME = 20;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path arquivo;

    private RegistroDeuses() {
    }

    public static synchronized void carregar(MinecraftServer servidor) {
        arquivo = net.aelysium.aelysiummod.sistemas.rp.PastaPersonagens.arquivo(servidor, "deuses_personalizados.json");
        TipoDeus.limparPersonalizados();
        carregarOriginais();
        if (!Files.exists(arquivo)) return;
        try {
            JsonArray lista = GSON.fromJson(Files.readString(arquivo, StandardCharsets.UTF_8), JsonArray.class);
            if (lista == null) return;
            for (JsonElement elemento : lista) {
                JsonObject objeto = elemento.getAsJsonObject();
                TipoDeus.registrarPersonalizado(objeto.get("id").getAsString(), objeto.get("nome").getAsString(), objeto.get("cor").getAsInt(),
                        objeto.has("badge") ? objeto.get("badge").getAsString() : null,
                        objeto.has("corParticula") ? objeto.get("corParticula").getAsInt() : -1,
                        objeto.has("pronomes") ? objeto.get("pronomes").getAsString() : null,
                        objeto.has("poderes") ? objeto.get("poderes").getAsInt() : TipoDeus.TODOS_PODERES);
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao carregar deuses_personalizados.json: ", e);
        }
    }

    private static Path arquivoOriginais() {
        Path antigo = arquivo.getParent().getParent().resolve("deuses_originais.json");
        Path novo = arquivo.resolveSibling("deuses_originais.json");
        try {
            if (!Files.exists(novo) && Files.exists(antigo)) Files.move(antigo, novo);
        } catch (java.io.IOException ignorado) {
        }
        return novo;
    }

    private static void carregarOriginais() {
        Path caminho = arquivoOriginais();
        if (!Files.exists(caminho)) return;
        try {
            JsonObject raiz = GSON.fromJson(Files.readString(caminho, StandardCharsets.UTF_8), JsonObject.class);
            if (raiz == null) return;
            if (raiz.has("editados")) {
                for (JsonElement elemento : raiz.getAsJsonArray("editados")) {
                    JsonObject objeto = elemento.getAsJsonObject();
                    TipoDeus.atualizar(objeto.get("id").getAsString(), objeto.get("nome").getAsString(), objeto.get("cor").getAsInt(),
                            objeto.has("badge") ? objeto.get("badge").getAsString() : null,
                            objeto.has("corParticula") ? objeto.get("corParticula").getAsInt() : -1,
                            objeto.has("pronomes") ? objeto.get("pronomes").getAsString() : null,
                            objeto.has("poderes") ? objeto.get("poderes").getAsInt() : TipoDeus.TODOS_PODERES);
                }
            }
            if (raiz.has("removidos")) {
                for (JsonElement elemento : raiz.getAsJsonArray("removidos")) {
                    TipoDeus.remover(elemento.getAsString());
                }
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao carregar deuses_originais.json: ", e);
        }
    }

    public static synchronized TipoDeus criar(String nome, int cor, String badge, int corParticula, String pronomes, int poderes) {
        String id = gerarId(nome);
        if (id.isEmpty()) return null;
        TipoDeus deus = TipoDeus.registrarPersonalizado(id, nome, cor, badge, corParticula, pronomes, poderes);
        if (deus != null) salvar();
        return deus;
    }

    public static synchronized boolean editar(String id, String nome, int cor, String badge, int corParticula, String pronomes, int poderes) {
        if (!TipoDeus.atualizar(id, nome, cor, badge, corParticula, pronomes, poderes)) return false;
        salvar();
        return true;
    }

    public static synchronized boolean excluir(String id) {
        if (!TipoDeus.remover(id)) return false;
        salvar();
        return true;
    }

    private static void salvar() {
        if (arquivo == null) return;
        try {
            Files.createDirectories(arquivo.getParent());
            JsonArray lista = new JsonArray();
            for (TipoDeus deus : TipoDeus.values()) {
                if (!deus.personalizado) continue;
                JsonObject objeto = new JsonObject();
                objeto.addProperty("id", deus.id);
                objeto.addProperty("nome", deus.displayName);
                objeto.addProperty("cor", deus.color);
                objeto.addProperty("badge", deus.badge);
                objeto.addProperty("corParticula", deus.corParticula);
                objeto.addProperty("pronomes", deus.pronomes);
                objeto.addProperty("poderes", deus.poderes);
                lista.add(objeto);
            }
            Files.writeString(arquivo, GSON.toJson(lista), StandardCharsets.UTF_8);
            JsonArray editados = new JsonArray();
            JsonArray removidos = new JsonArray();
            for (TipoDeus original : TipoDeus.originais()) {
                if (original == TipoDeus.NONE) continue;
                if (TipoDeus.removido(original)) {
                    removidos.add(original.id);
                } else if (TipoDeus.editado(original)) {
                    JsonObject objeto = new JsonObject();
                    objeto.addProperty("id", original.id);
                    objeto.addProperty("nome", original.displayName);
                    objeto.addProperty("cor", original.color);
                    objeto.addProperty("badge", original.badge);
                    objeto.addProperty("corParticula", original.corParticula);
                    objeto.addProperty("pronomes", original.pronomes);
                    objeto.addProperty("poderes", original.poderes);
                    editados.add(objeto);
                }
            }
            JsonObject raiz = new JsonObject();
            raiz.add("editados", editados);
            raiz.add("removidos", removidos);
            Files.writeString(arquivoOriginais(), GSON.toJson(raiz), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao salvar deuses_personalizados.json: ", e);
        }
    }

    public static String gerarId(String nome) {
        String normalizado = Normalizer.normalize(nome, Normalizer.Form.NFD);
        StringBuilder id = new StringBuilder();
        for (char c : normalizado.toCharArray()) {
            if (Character.getType(c) == Character.NON_SPACING_MARK) continue;
            char minusculo = Character.toLowerCase(c);
            if ((minusculo >= 'a' && minusculo <= 'z') || (minusculo >= '0' && minusculo <= '9')) {
                id.append(minusculo);
            } else if (id.length() > 0 && id.charAt(id.length() - 1) != '_') {
                id.append('_');
            }
        }
        while (id.length() > 0 && id.charAt(id.length() - 1) == '_') id.setLength(id.length() - 1);
        return id.toString();
    }

    @SubscribeEvent
    public static void aoPararServidor(ServerStoppedEvent evento) {
        TipoDeus.limparPersonalizados();
        arquivo = null;
    }
}
