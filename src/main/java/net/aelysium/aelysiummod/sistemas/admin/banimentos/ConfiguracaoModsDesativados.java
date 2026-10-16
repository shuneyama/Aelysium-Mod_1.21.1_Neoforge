package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.armazenamento.PastaAelysium;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfiguracaoModsDesativados {

    public static class ModDesativado {
        public boolean loot = false;
        public boolean trocas = false;
        public boolean receitas = false;
        public boolean worldgen = false;
        public boolean conquistas = false;

        public ModDesativado() {}

        public ModDesativado(boolean loot, boolean trocas, boolean receitas, boolean worldgen, boolean conquistas) {
            this.loot = loot;
            this.trocas = trocas;
            this.receitas = receitas;
            this.worldgen = worldgen;
            this.conquistas = conquistas;
        }
    }

    private static Map<String, ModDesativado> caps = new LinkedHashMap<>();
    private static volatile boolean algumWorldgen = false;

    public static Path getConfigFile() {
        return PastaAelysium.arquivoMigrado("mods_nerfados.toml", "mods_capados.toml");
    }

    public static synchronized void load() {
        if (!PastaAelysium.pronta()) return;
        Path p = getConfigFile();
        caps = new LinkedHashMap<>();

        if (!Files.exists(p)) {
            save();
            return;
        }

        try {
            CommentedFileConfig cfg = CommentedFileConfig.builder(p).charset(StandardCharsets.UTF_8).build();
            cfg.load();

            Object modsObj = cfg.get("mods");
            if (modsObj instanceof Config modsCfg) {
                for (Config.Entry entry : modsCfg.entrySet()) {
                    Object val = entry.getValue();
                    if (val instanceof Config modCfg) {
                        caps.put(entry.getKey(), new ModDesativado(
                                modCfg.getOrElse("loot", false),
                                modCfg.getOrElse("trocas", false),
                                modCfg.getOrElse("receitas", false),
                                modCfg.getOrElse("worldgen", false),
                                modCfg.getOrElse("conquistas", false)));
                    }
                }
            }

            cfg.close();
        } catch (Exception e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao carregar mods_nerfados.toml: ", e);
        }
        atualizarIndices();
    }

    public static synchronized void save() {
        if (!PastaAelysium.pronta()) return;
        Path p = getConfigFile();
        try {
            Files.createDirectories(p.getParent());
            StringBuilder sb = new StringBuilder();

            sb.append("# Sistema de \"nerf\" de mods por ID.\n");
            sb.append("# Desativa partes inteiras de um mod (por id) sem precisar editar o mod em si.\n");
            sb.append("#\n");
            sb.append("# loot       = remove itens desse mod de: baús, vaults, barris, drops extras injetados\n");
            sb.append("#              em mobs vanilla (via loot modifiers), drops dos mobs do próprio mod ao\n");
            sb.append("#              morrer, e drops de blocos do próprio mod ao serem quebrados.\n");
            sb.append("# trocas     = remove trocas com o Mercador Errante, trocas adicionadas a vilarões\n");
            sb.append("#              vanilla, e bloqueia interação com NPCs mercadores do próprio mod.\n");
            sb.append("# receitas   = remove as receitas desse mod (as do próprio mod e as que resultam em itens dele).\n");
            sb.append("# worldgen   = impede a geração de features (minérios, árvores, plantas...) e estruturas desse mod\n");
            sb.append("#              em chunks que ainda não foram gerados.\n");
            sb.append("# conquistas = remove as conquistas (advancements) desse mod.\n");
            sb.append("#\n");
            sb.append("# \"loot\" e \"receitas\" aplicam em tempo real (ou via /aelysium capar-mod reload).\n");
            sb.append("# \"trocas\" e \"conquistas\" exigem /reload (datapack) ou reiniciar o servidor.\n");
            sb.append("# \"worldgen\" vale para chunks gerados a partir de agora.\n");
            sb.append("#\n");
            sb.append("# Exemplo:\n");
            sb.append("# [mods.ironsspellbooks]\n");
            sb.append("# loot = true\n");
            sb.append("# trocas = true\n");
            sb.append("# receitas = true\n");
            sb.append("# worldgen = true\n");
            sb.append("# conquistas = true\n");

            for (Map.Entry<String, ModDesativado> entry : caps.entrySet()) {
                ModDesativado cap = entry.getValue();
                sb.append("\n[mods.").append(tomlKey(entry.getKey())).append("]\n");
                sb.append("loot = ").append(cap.loot).append("\n");
                sb.append("trocas = ").append(cap.trocas).append("\n");
                sb.append("receitas = ").append(cap.receitas).append("\n");
                sb.append("worldgen = ").append(cap.worldgen).append("\n");
                sb.append("conquistas = ").append(cap.conquistas).append("\n");
            }

            Files.writeString(p, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao salvar mods_nerfados.toml: ", e);
        }
        atualizarIndices();
    }

    private static void atualizarIndices() {
        algumWorldgen = caps.values().stream().anyMatch(c -> c.worldgen);
    }

    private static String tomlKey(String key) {
        if (key.matches("[a-zA-Z0-9_-]+")) return key;
        return "\"" + key.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    public static synchronized void setCap(String modId, boolean loot, boolean trocas) {
        ModDesativado atual = caps.getOrDefault(modId.toLowerCase(), new ModDesativado());
        setCap(modId, loot, trocas, atual.receitas, atual.worldgen, atual.conquistas);
    }

    public static synchronized void setCap(String modId, boolean loot, boolean trocas, boolean receitas, boolean worldgen, boolean conquistas) {
        caps.put(modId.toLowerCase(), new ModDesativado(loot, trocas, receitas, worldgen, conquistas));
        save();
    }

    public static synchronized boolean removeCap(String modId) {
        boolean removed = caps.remove(modId.toLowerCase()) != null;
        if (removed) save();
        return removed;
    }

    public static boolean isLootCapped(String modId) {
        ModDesativado cap = caps.get(modId);
        return cap != null && cap.loot;
    }

    public static boolean isTrocasCapped(String modId) {
        ModDesativado cap = caps.get(modId);
        return cap != null && cap.trocas;
    }

    public static boolean isReceitasCapped(String modId) {
        ModDesativado cap = caps.get(modId);
        return cap != null && cap.receitas;
    }

    public static boolean isWorldgenCapped(String modId) {
        ModDesativado cap = caps.get(modId);
        return cap != null && cap.worldgen;
    }

    public static boolean isConquistasCapped(String modId) {
        ModDesativado cap = caps.get(modId);
        return cap != null && cap.conquistas;
    }

    public static boolean haWorldgenCapado() {
        return algumWorldgen;
    }

    public static Map<String, ModDesativado> getAll() {
        return Collections.unmodifiableMap(caps);
    }
}
