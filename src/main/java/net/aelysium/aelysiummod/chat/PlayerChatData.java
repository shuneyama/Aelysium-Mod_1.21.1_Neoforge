package net.aelysium.aelysiummod.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerChatData {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, PlayerChatData> CACHE = new ConcurrentHashMap<>();
    private static Path dataFolder;

    public int corTexto = 0x141414;
    public int corFundo = 0xFFFFFF;
    public int corBorda = 0x000000;
    public float altura = BalloonConfig.BALLOON_HEIGHT_OFFSET;
    public BalloonStyle estilo = BalloonStyle.ROUNDED;

    public static void setDataFolder(Path folder) {
        dataFolder = folder;
        try {
            Files.createDirectories(folder);
        } catch (IOException ignored) {}
    }

    public static PlayerChatData get(UUID uuid) {
        return CACHE.computeIfAbsent(uuid, id -> load(id));
    }

    public static void save(UUID uuid) {
        PlayerChatData data = CACHE.get(uuid);
        if (data == null || dataFolder == null) return;

        try {
            Path file = dataFolder.resolve(uuid.toString() + ".json");
            Files.writeString(file, GSON.toJson(data));
        } catch (IOException ignored) {}
    }

    private static PlayerChatData load(UUID uuid) {
        if (dataFolder == null) return new PlayerChatData();

        Path file = dataFolder.resolve(uuid.toString() + ".json");
        if (Files.exists(file)) {
            try {
                return GSON.fromJson(Files.readString(file), PlayerChatData.class);
            } catch (IOException ignored) {}
        }
        return new PlayerChatData();
    }

    public static void clearCache(UUID uuid) {
        CACHE.remove(uuid);
    }
}
