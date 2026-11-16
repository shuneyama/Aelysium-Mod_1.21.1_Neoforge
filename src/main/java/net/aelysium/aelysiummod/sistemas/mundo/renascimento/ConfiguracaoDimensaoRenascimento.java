package net.aelysium.aelysiummod.sistemas.mundo.renascimento;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.gson.*;
import net.aelysium.aelysiummod.nucleo.armazenamento.PastaAelysium;
import net.aelysium.aelysiummod.sistemas.admin.historico.ConfiguracaoRegrasMods;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfiguracaoDimensaoRenascimento {

    private static final Logger LOGGER = LoggerFactory.getLogger("aelysiummod");

    private static boolean definido = false;
    private static ResourceKey<Level> dimensao = Level.OVERWORLD;
    private static double x = 0, y = 0, z = 0;
    private static float yaw = 0;

    public static void load(MinecraftServer server) {
        definido = false;
        dimensao = Level.OVERWORLD;
        x = 0; y = 0; z = 0; yaw = 0;

        JsonObject salvo = ConfiguracaoRegrasMods.spawnMundo();
        if (salvo != null) {
            try {
                ResourceLocation loc = ResourceLocation.tryParse(salvo.get("dimensao").getAsString());
                if (loc != null) {
                    dimensao = ResourceKey.create(Registries.DIMENSION, loc);
                    x = salvo.get("x").getAsDouble();
                    y = salvo.get("y").getAsDouble();
                    z = salvo.get("z").getAsDouble();
                    yaw = salvo.has("angulo") ? salvo.get("angulo").getAsFloat() : 0;
                    definido = true;
                }
            } catch (Exception e) {
                LOGGER.error("Erro ao carregar o spawnMundo do regras_mods.json: ", e);
            }
            return;
        }
        migrarArquivoProprio();
        if (!definido) migrarDeVersoesAnteriores(server);
    }

    private static void migrarArquivoProprio() {
        Path arquivo = PastaAelysium.arquivo("spawn_mundo.json");
        if (!Files.exists(arquivo)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(arquivo, StandardCharsets.UTF_8)).getAsJsonObject();
            ResourceLocation loc = ResourceLocation.tryParse(root.get("dimensao").getAsString());
            if (loc != null) {
                definirSemSalvar(ResourceKey.create(Registries.DIMENSION, loc), root.get("x").getAsDouble(),
                        root.get("y").getAsDouble(), root.get("z").getAsDouble(), root.has("angulo") ? root.get("angulo").getAsFloat() : 0);
                save();
            }
            Files.deleteIfExists(arquivo);
        } catch (Exception e) {
            LOGGER.error("Erro ao migrar spawn_mundo.json: ", e);
        }
    }

    private static void migrarDeVersoesAnteriores(MinecraftServer server) {
        Path raiz = server.getWorldPath(LevelResource.ROOT);
        Path toml = PastaAelysium.arquivo("spawn.toml");
        try {
            if (Files.exists(toml)) {
                CommentedFileConfig cfg = CommentedFileConfig.builder(toml).charset(StandardCharsets.UTF_8).build();
                cfg.load();
                if (cfg.getOrElse("tem_coordenada", false)) {
                    ResourceLocation loc = ResourceLocation.tryParse(cfg.getOrElse("dimensao_padrao", "minecraft:overworld"));
                    if (loc != null) {
                        definirSemSalvar(ResourceKey.create(Registries.DIMENSION, loc),
                                cfg.<Number>getOrElse("x", 0).doubleValue(), cfg.<Number>getOrElse("y", 0).doubleValue(),
                                cfg.<Number>getOrElse("z", 0).doubleValue(), cfg.<Number>getOrElse("yaw", 0).floatValue());
                    }
                }
                cfg.close();
                Files.deleteIfExists(toml);
            }
            for (Path antigo : new Path[]{PastaAelysium.arquivo("spawn.properties"), raiz.resolve("aelysium_spawn.properties")}) {
                if (!Files.exists(antigo)) continue;
                Properties props = new Properties();
                try (InputStream in = Files.newInputStream(antigo)) {
                    props.load(in);
                }
                if (!definido && Boolean.parseBoolean(props.getProperty("temCoordenada", "false"))) {
                    ResourceLocation loc = ResourceLocation.tryParse(props.getProperty("dimensaoPadrao", "minecraft:overworld"));
                    if (loc != null) {
                        definirSemSalvar(ResourceKey.create(Registries.DIMENSION, loc),
                                Double.parseDouble(props.getProperty("x", "0")), Double.parseDouble(props.getProperty("y", "0")),
                                Double.parseDouble(props.getProperty("z", "0")), Float.parseFloat(props.getProperty("yaw", "0")));
                    }
                }
                Files.deleteIfExists(antigo);
            }
        } catch (Exception e) {
            LOGGER.error("Erro ao migrar o spawn de versoes anteriores: ", e);
            return;
        }
        if (definido) {
            save();
        }
    }

    private static void definirSemSalvar(ResourceKey<Level> novaDimensao, double novoX, double novoY, double novoZ, float novoYaw) {
        definido = true;
        dimensao = novaDimensao;
        x = novoX;
        y = novoY;
        z = novoZ;
        yaw = novoYaw;
    }

    public static void save() {
        if (!definido) return;
        JsonObject root = new JsonObject();
        root.addProperty("dimensao", dimensao.location().toString());
        root.addProperty("x", x);
        root.addProperty("y", y);
        root.addProperty("z", z);
        root.addProperty("angulo", yaw);
        ConfiguracaoRegrasMods.definirSpawnMundo(root);
    }

    public static void definir(ResourceKey<Level> novaDimensao, double novoX, double novoY, double novoZ, float novoYaw) {
        definirSemSalvar(novaDimensao, novoX, novoY, novoZ, novoYaw);
        save();
    }

    public static boolean definido() {
        return definido;
    }

    public static ResourceKey<Level> getDimensaoPadrao() {
        return dimensao;
    }

    public static double getX() {
        return x;
    }

    public static double getY() {
        return y;
    }

    public static double getZ() {
        return z;
    }

    public static float getYaw() {
        return yaw;
    }
}
