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
    public static final int TAMANHO_MAXIMO_NOME = 16;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path arquivo;

    private RegistroDeuses() {
    }

    public static synchronized void carregar(MinecraftServer servidor) {
        arquivo = servidor.getWorldPath(LevelResource.ROOT).resolve("aelysium").resolve("deuses_personalizados.json");
        TipoDeus.limparPersonalizados();
        if (!Files.exists(arquivo)) return;
        try {
            JsonArray lista = GSON.fromJson(Files.readString(arquivo, StandardCharsets.UTF_8), JsonArray.class);
            if (lista == null) return;
            for (JsonElement elemento : lista) {
                JsonObject objeto = elemento.getAsJsonObject();
                TipoDeus.registrarPersonalizado(objeto.get("id").getAsString(), objeto.get("nome").getAsString(), objeto.get("cor").getAsInt());
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao carregar deuses_personalizados.json: ", e);
        }
    }

    public static synchronized TipoDeus criar(String nome, int cor) {
        String id = gerarId(nome);
        if (id.isEmpty()) return null;
        TipoDeus deus = TipoDeus.registrarPersonalizado(id, nome, cor);
        if (deus != null) salvar();
        return deus;
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
                lista.add(objeto);
            }
            Files.writeString(arquivo, GSON.toJson(lista), StandardCharsets.UTF_8);
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
