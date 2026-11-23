package net.aelysium.aelysiummod.nucleo.configuracao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfiguracaoChat {
    public static final int RAIO_PADRAO = 40;
    public static final int ANTISPAM_PADRAO_SEGUNDOS = 5;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path arquivo;
    private static int localChatRadius = RAIO_PADRAO;
    private static int antiSpamSegundos = ANTISPAM_PADRAO_SEGUNDOS;

    public static synchronized void carregar(MinecraftServer servidor) {
        arquivo = servidor.getWorldPath(LevelResource.ROOT).resolve("aelysium").resolve("chat.json");
        localChatRadius = RAIO_PADRAO;
        antiSpamSegundos = ANTISPAM_PADRAO_SEGUNDOS;
        if (!Files.exists(arquivo)) {
            salvar();
            return;
        }
        try {
            JsonObject json = GSON.fromJson(Files.readString(arquivo, StandardCharsets.UTF_8), JsonObject.class);
            if (json == null) return;
            if (json.has("raio")) localChatRadius = json.get("raio").getAsInt();
            if (json.has("antispam_segundos")) antiSpamSegundos = json.get("antispam_segundos").getAsInt();
        } catch (Exception e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao carregar chat.json: ", e);
        }
    }

    private static synchronized void salvar() {
        if (arquivo == null) return;
        try {
            Files.createDirectories(arquivo.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("raio", localChatRadius);
            json.addProperty("antispam_segundos", antiSpamSegundos);
            Files.writeString(arquivo, GSON.toJson(json), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao salvar chat.json: ", e);
        }
    }

    public static synchronized int getLocalChatRadius() {
        return localChatRadius;
    }

    public static synchronized void setLocalChatRadius(int radius) {
        localChatRadius = radius;
        salvar();
    }

    public static synchronized int getAntiSpamSegundos() {
        return antiSpamSegundos;
    }

    public static synchronized void setAntiSpamSegundos(int segundos) {
        antiSpamSegundos = segundos;
        salvar();
    }
}
