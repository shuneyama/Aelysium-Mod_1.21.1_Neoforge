package net.aelysium.aelysiummod.comandos.racas;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.aelysium.aelysiummod.util.VerificarConfigs;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.List;

public class Undyne_Config {

    public static ConfigData DATA;
    private static final String CONFIG_NAME = "undyne.json";

    public static class ConfigData {
        public Team team;
        public Attributes status;
        public Attributes attributes;
        public Effects effects;
    }

    public static class Team {
        public boolean enabled;
        public String name;
    }

    public static class Attributes {
        public boolean enabled;
        public List<AttributeEntry> list;
    }

    public static class AttributeEntry {
        public String attribute;
        public double value;
        public AttributeEntry(String attribute, double value) {
            this.attribute = attribute;
            this.value = value;
        }
    }

    public static class Effects {
        public boolean enabled;
        public List<EffectEntry> list;
    }

    public static class EffectEntry {
        public String effect;
        public int duration;
        public int amplifier;
        public EffectEntry(String effect, int duration, int amplifier) {
            this.effect = effect;
            this.duration = duration;
            this.amplifier = amplifier;
        }
    }

    public static void load(MinecraftServer server) {
        System.out.println("[Aelysium] === Carregando UNDYNE ===");
        try {
            File file = VerificarConfigs.getConfigFile(server, CONFIG_NAME);
            if (!file.exists()) {
                VerificarConfigs.ensureConfigDirectory(file);
                generateDefault(file);
            }
            if (file.exists()) {
                Gson gson = new Gson();
                Type type = new TypeToken<ConfigData>(){}.getType();
                try (FileReader reader = new FileReader(file)) {
                    DATA = gson.fromJson(reader, type);
                    System.out.println("[Aelysium] ✓ Carregado!");
                }
            }
        } catch (Exception e) {
            System.out.println("[Aelysium] ✗ ERRO:");
            e.printStackTrace();
        }
    }

    public static void loadClient() {
        try {
            File file = new File("config/aelysium/" + CONFIG_NAME);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                generateDefault(file);
            }
            if (DATA == null) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Type type = new TypeToken<ConfigData>(){}.getType();
                DATA = gson.fromJson(new FileReader(file), type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateDefault(File file) {
        try {
            ConfigData defaultConfig = new ConfigData();
            defaultConfig.team = new Team();
            defaultConfig.team.enabled = true;
            defaultConfig.team.name = "undynes";

            defaultConfig.status = new Attributes();
            defaultConfig.status.enabled = true;
            defaultConfig.status.list = List.of(
                    new AttributeEntry("minecraft:generic.max_health", 16.0),
                    new AttributeEntry("minecraft:generic.armor", 0.0),
                    new AttributeEntry("irons_spellbooks:ice_spell_power", 1.05),
                    new AttributeEntry("minecraft:generic.attack_damage", 2.0)
            );

            defaultConfig.attributes = new Attributes();
            defaultConfig.attributes.enabled = true;
            defaultConfig.attributes.list = List.of(
                    new AttributeEntry("minecraft:generic.attack_speed", 4.5),
                    new AttributeEntry("neoforge:swim_speed", 1.5),
                    new AttributeEntry("minecraft:player.submerged_mining_speed", 1.0),
                    new AttributeEntry("irons_spellbooks:ice_magic_resist", 1.05),
                    new AttributeEntry("irons_spellbooks:lightning_magic_resist", 0.95)
            );

            defaultConfig.effects = new Effects();
            defaultConfig.effects.enabled = true;
            defaultConfig.effects.list = List.of(
                    new EffectEntry("minecraft:water_breathing", 120, 0)
            );

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(gson.toJson(defaultConfig));
                writer.flush();
            }
            System.out.println("[Aelysium] ✓ Criado: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Holder<MobEffect> getEffect(String id) {
        try {
            return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(id)).orElse(null);
        } catch (Exception e) { return null; }
    }

    public static Holder<Attribute> getAttribute(String id) {
        try {
            return BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(id)).orElse(null);
        } catch (Exception e) { return null; }
    }
}