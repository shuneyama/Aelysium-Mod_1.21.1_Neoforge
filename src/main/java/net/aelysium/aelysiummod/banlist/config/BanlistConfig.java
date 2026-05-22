package net.aelysium.aelysiummod.banlist.config;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BanlistConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Set<ResourceLocation> BANNED_ITEMS = ConcurrentHashMap.newKeySet();

    private static boolean showTooltip = true;
    private static String tooltipBanned = "§cVocê não pode utilizar esse item.";
    private static String tooltipAllowed = "§aEsse item foi confiado à você.";
    private static boolean silentCraftBlock = false;
    private static String craftBlockMessage = "§cVocê não pode craftar este item.";

    private static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get().resolve("banlist");
    }

    private static Path getConfigFile() {
        return getConfigDir().resolve("itemban.json");
    }

    public static void load() {
        Path file = getConfigFile();

        if (!Files.exists(file)) {
            createDefault();
            return;
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            BANNED_ITEMS.clear();

            if (root.has("banned_items")) {
                JsonArray arr = root.getAsJsonArray("banned_items");
                for (JsonElement el : arr) {
                    String id = el.getAsString();
                    ResourceLocation rl = ResourceLocation.tryParse(id);
                    if (rl != null) {
                        BANNED_ITEMS.add(rl);
                    } else {
                        LOGGER.warn("[Banlist] ID de item inválido na config: {}", id);
                    }
                }
            }

            if (root.has("settings")) {
                JsonObject settings = root.getAsJsonObject("settings");
                showTooltip = getOrDefault(settings, "show_tooltip", true);
                tooltipBanned = getOrDefault(settings, "tooltip_banned", "§cVocê não pode utilizar esse item.");
                tooltipAllowed = getOrDefault(settings, "tooltip_allowed", "§aEsse item foi confiado à você.");
                silentCraftBlock = getOrDefault(settings, "silent_craft_block", false);
                craftBlockMessage = getOrDefault(settings, "craft_block_message", "§cVocê não pode craftar este item.");
            }

            LOGGER.info("[Banlist] Config carregada: {} itens banidos.", BANNED_ITEMS.size());

        } catch (Exception e) {
            LOGGER.error("[Banlist] Erro ao carregar config: {}", e.getMessage());
        }
    }

    public static void save() {
        Path file = getConfigFile();

        try {
            Files.createDirectories(file.getParent());

            JsonObject root = new JsonObject();

            JsonArray arr = new JsonArray();
            List<String> sorted = new ArrayList<>();
            for (ResourceLocation rl : BANNED_ITEMS) {
                sorted.add(rl.toString());
            }
            Collections.sort(sorted);
            for (String s : sorted) {
                arr.add(s);
            }
            root.add("banned_items", arr);

            JsonObject settings = new JsonObject();
            settings.addProperty("show_tooltip", showTooltip);
            settings.addProperty("tooltip_banned", tooltipBanned);
            settings.addProperty("tooltip_allowed", tooltipAllowed);
            settings.addProperty("silent_craft_block", silentCraftBlock);
            settings.addProperty("craft_block_message", craftBlockMessage);
            root.add("settings", settings);

            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }

        } catch (Exception e) {
            LOGGER.error("[Banlist] Erro ao salvar config: {}", e.getMessage());
        }
    }

    private static void createDefault() {
        BANNED_ITEMS.clear();
        save();
        LOGGER.info("[Banlist] Config padrão criada em config/banlist/itemban.json");
    }

    public static Set<ResourceLocation> getBannedItems() {
        return Collections.unmodifiableSet(BANNED_ITEMS);
    }

    public static boolean isBanned(ResourceLocation itemId) {
        return BANNED_ITEMS.contains(itemId);
    }

    public static boolean isShowTooltip() {
        return showTooltip;
    }

    public static String getTooltipBanned() {
        return tooltipBanned;
    }

    public static String getTooltipAllowed() {
        return tooltipAllowed;
    }

    public static boolean isSilentCraftBlock() {
        return silentCraftBlock;
    }

    public static String getCraftBlockMessage() {
        return craftBlockMessage;
    }

    public static boolean addBannedItem(ResourceLocation itemId) {
        boolean added = BANNED_ITEMS.add(itemId);
        if (added) save();
        return added;
    }

    public static boolean removeBannedItem(ResourceLocation itemId) {
        boolean removed = BANNED_ITEMS.remove(itemId);
        if (removed) save();
        return removed;
    }

    private static boolean getOrDefault(JsonObject obj, String key, boolean def) {
        return obj.has(key) ? obj.get(key).getAsBoolean() : def;
    }

    private static String getOrDefault(JsonObject obj, String key, String def) {
        return obj.has(key) ? obj.get(key).getAsString() : def;
    }
}
