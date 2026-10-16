package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.aelysium.aelysiummod.nucleo.armazenamento.PastaAelysium;
import net.aelysium.aelysiummod.nucleo.configuracao.AelysiumDiretorioConfiguracao;
import net.aelysium.aelysiummod.sistemas.admin.historico.FormatoDataHistorico;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfiguracaoBanimentoReceitas {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record Remocao(String por, String porUuid, String em) {}

    private static final Map<ResourceLocation, Remocao> BLOCKED_ITEMS = new ConcurrentHashMap<>();

    private static Path getPasta() {
        return PastaAelysium.pasta("receitas_removidas");
    }

    public static synchronized void load() {
        BLOCKED_ITEMS.clear();
        if (!PastaAelysium.pronta()) return;

        Path pasta = getPasta();
        if (Files.isDirectory(pasta)) {
            try (var stream = Files.list(pasta)) {
                for (Path arquivo : stream.filter(p -> p.getFileName().toString().endsWith(".json")).toList()) {
                    lerArquivo(arquivo);
                }
            } catch (IOException e) {
                LOGGER.error("[RecipeBan] Erro ao listar {}: {}", pasta, e.getMessage());
            }
        }
        migrarDoToml();
        LOGGER.info("[RecipeBan] {} receitas removidas.", BLOCKED_ITEMS.size());
    }

    private static void lerArquivo(Path arquivo) {
        try {
            JsonObject root = JsonParser.parseString(Files.readString(arquivo, StandardCharsets.UTF_8)).getAsJsonObject();
            for (JsonElement el : root.getAsJsonArray("itens")) {
                JsonObject obj = el.getAsJsonObject();
                ResourceLocation rl = ResourceLocation.tryParse(obj.get("item").getAsString());
                if (rl == null) continue;
                BLOCKED_ITEMS.put(rl, new Remocao(
                        obj.has("por") ? obj.get("por").getAsString() : "desconhecido",
                        obj.has("porUuid") ? obj.get("porUuid").getAsString() : "",
                        obj.has("em") ? obj.get("em").getAsString() : ""));
            }
        } catch (Exception e) {
            LOGGER.error("[RecipeBan] Erro ao ler {}: {}", arquivo, e.getMessage());
        }
    }

    private static void migrarDoToml() {
        Path toml = AelysiumDiretorioConfiguracao.file("receitas_banidas.toml");
        if (!Files.exists(toml)) return;
        try {
            CommentedFileConfig cfg = CommentedFileConfig.builder(toml).charset(StandardCharsets.UTF_8).build();
            cfg.load();
            List<String> lista = cfg.getOrElse("receitas_bloqueadas", Collections.emptyList());
            cfg.close();

            Set<String> mods = new TreeSet<>();
            for (String id : lista) {
                ResourceLocation rl = ResourceLocation.tryParse(id);
                if (rl == null) continue;
                BLOCKED_ITEMS.putIfAbsent(rl, new Remocao("desconhecido", "", ""));
                mods.add(rl.getNamespace());
            }
            boolean ok = true;
            for (String mod : mods) ok &= salvarMod(mod);
            if (ok) {
                Files.deleteIfExists(toml);
                LOGGER.info("[RecipeBan] receitas_banidas.toml convertido em {} arquivos e apagado.", mods.size());
            }
        } catch (Exception e) {
            LOGGER.error("[RecipeBan] Erro ao migrar receitas_banidas.toml: {}", e.getMessage());
        }
    }

    private static synchronized boolean salvarMod(String mod) {
        if (!PastaAelysium.pronta()) return false;
        Path arquivo = getPasta().resolve(mod + ".json");
        try {
            JsonArray itens = new JsonArray();
            BLOCKED_ITEMS.entrySet().stream()
                    .filter(e -> e.getKey().getNamespace().equals(mod))
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("item", e.getKey().toString());
                        obj.addProperty("por", e.getValue().por());
                        obj.addProperty("porUuid", e.getValue().porUuid());
                        obj.addProperty("em", e.getValue().em());
                        itens.add(obj);
                    });
            if (itens.isEmpty()) {
                Files.deleteIfExists(arquivo);
                return true;
            }
            JsonObject root = new JsonObject();
            root.addProperty("mod", mod);
            root.add("itens", itens);
            Files.createDirectories(arquivo.getParent());
            Files.writeString(arquivo, GSON.toJson(root), StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            LOGGER.error("[RecipeBan] Erro ao salvar {}: {}", arquivo, e.getMessage());
            return false;
        }
    }

    public static Set<ResourceLocation> getBlockedItems() {
        return Collections.unmodifiableSet(BLOCKED_ITEMS.keySet());
    }

    public static Map<ResourceLocation, Remocao> getRemocoes() {
        return Collections.unmodifiableMap(BLOCKED_ITEMS);
    }

    public static boolean isBlocked(ResourceLocation itemId) {
        return BLOCKED_ITEMS.containsKey(itemId);
    }

    public static boolean addBlockedItem(ResourceLocation itemId) {
        return addBlockedItem(itemId, "desconhecido", null);
    }

    public static boolean addBlockedItem(ResourceLocation itemId, String por, UUID porUuid) {
        if (BLOCKED_ITEMS.containsKey(itemId)) return false;
        BLOCKED_ITEMS.put(itemId, new Remocao(por, porUuid == null ? "" : porUuid.toString(), FormatoDataHistorico.agora()));
        salvarMod(itemId.getNamespace());
        return true;
    }

    public static boolean removeBlockedItem(ResourceLocation itemId) {
        boolean removed = BLOCKED_ITEMS.remove(itemId) != null;
        if (removed) salvarMod(itemId.getNamespace());
        return removed;
    }
}
