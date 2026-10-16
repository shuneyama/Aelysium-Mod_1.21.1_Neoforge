package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.aelysium.aelysiummod.nucleo.configuracao.AelysiumDiretorioConfiguracao;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfiguracaoListaBanimentos {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Set<ResourceLocation> BANNED_ITEMS = ConcurrentHashMap.newKeySet();
    private static MinecraftServer servidor;

    private static final boolean SHOW_TOOLTIP = true;
    private static final String TOOLTIP_BANNED = "§cVocê não pode utilizar esse item.";
    private static final String TOOLTIP_ALLOWED = "§aEsse item foi confiado à você.";
    private static final boolean SILENT_CRAFT_BLOCK = false;
    private static final String CRAFT_BLOCK_MESSAGE = "§cVocê não pode craftar este item.";

    private static Path getArquivo() {
        Path pasta = servidor.getWorldPath(LevelResource.ROOT).resolve("aelysium").resolve("itens_banidos");
        net.aelysium.aelysiummod.nucleo.armazenamento.MigracaoPasta.moverPasta(pasta.resolveSibling("banlist"), pasta);
        net.aelysium.aelysiummod.nucleo.armazenamento.MigracaoPasta.moverArquivo(pasta.resolve("itens_banidos.json"), pasta.resolve("geral.json"));
        return pasta.resolve("geral.json");
    }

    public static void load(MinecraftServer server) {
        servidor = server;
        load();
    }

    public static void load() {
        if (servidor == null) return;
        Path arquivo = getArquivo();

        if (!Files.exists(arquivo)) {
            migrarDoToml();
            return;
        }

        try {
            JsonObject root = JsonParser.parseString(Files.readString(arquivo, StandardCharsets.UTF_8)).getAsJsonObject();
            BANNED_ITEMS.clear();
            if (root.has("itensBanidos")) {
                for (JsonElement el : root.getAsJsonArray("itensBanidos")) {
                    ResourceLocation rl = ResourceLocation.tryParse(el.getAsString());
                    if (rl != null) {
                        BANNED_ITEMS.add(rl);
                    } else {
                        LOGGER.warn("[Banlist] ID de item inválido na config: {}", el.getAsString());
                    }
                }
            }
            LOGGER.info("[Banlist] Config carregada: {} itens banidos.", BANNED_ITEMS.size());
        } catch (Exception e) {
            LOGGER.error("[Banlist] Erro ao carregar config: {}", e.getMessage());
        }
    }

    private static void migrarDoToml() {
        BANNED_ITEMS.clear();
        Path toml = AelysiumDiretorioConfiguracao.file("itens_banidos.toml");
        if (Files.exists(toml)) {
            try {
                CommentedFileConfig cfg = CommentedFileConfig.builder(toml).charset(StandardCharsets.UTF_8).build();
                cfg.load();
                List<String> lista = cfg.getOrElse("itens_banidos", Collections.emptyList());
                for (String id : lista) {
                    ResourceLocation rl = ResourceLocation.tryParse(id);
                    if (rl != null) BANNED_ITEMS.add(rl);
                }
                cfg.close();
            } catch (Exception e) {
                LOGGER.error("[Banlist] Erro ao migrar itens_banidos.toml: {}", e.getMessage());
                return;
            }
        }
        save();
        if (Files.exists(toml) && Files.exists(getArquivo())) {
            try {
                Files.delete(toml);
                LOGGER.info("[Banlist] itens_banidos.toml convertido ({} itens banidos) e apagado.", BANNED_ITEMS.size());
            } catch (IOException e) {
                LOGGER.error("[Banlist] Erro ao apagar itens_banidos.toml: {}", e.getMessage());
            }
        }
    }

    public static void save() {
        if (servidor == null) return;
        Path arquivo = getArquivo();
        try {
            Files.createDirectories(arquivo.getParent());

            JsonArray itens = new JsonArray();
            BANNED_ITEMS.stream().map(ResourceLocation::toString).sorted().forEach(itens::add);
            JsonObject root = new JsonObject();
            root.add("itensBanidos", itens);

            Files.writeString(arquivo, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[Banlist] Erro ao salvar config: {}", e.getMessage());
        }
    }

    public static Set<ResourceLocation> getBannedItems() {
        return Collections.unmodifiableSet(BANNED_ITEMS);
    }

    public static boolean isBanned(ResourceLocation itemId) {
        return BANNED_ITEMS.contains(itemId);
    }

    public static boolean isShowTooltip() { return SHOW_TOOLTIP; }
    public static String getTooltipBanned() { return TOOLTIP_BANNED; }
    public static String getTooltipAllowed() { return TOOLTIP_ALLOWED; }
    public static boolean isSilentCraftBlock() { return SILENT_CRAFT_BLOCK; }
    public static String getCraftBlockMessage() { return CRAFT_BLOCK_MESSAGE; }

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

    public static int addBannedItems(Collection<ResourceLocation> itemIds) {
        int added = 0;
        for (ResourceLocation itemId : itemIds) {
            if (BANNED_ITEMS.add(itemId)) added++;
        }
        if (added > 0) save();
        return added;
    }

    public static int removeBannedItems(Collection<ResourceLocation> itemIds) {
        int removed = 0;
        for (ResourceLocation itemId : itemIds) {
            if (BANNED_ITEMS.remove(itemId)) removed++;
        }
        if (removed > 0) save();
        return removed;
    }
}
