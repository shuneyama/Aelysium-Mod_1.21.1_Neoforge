package net.aelysium.aelysiummod.config;

import java.io.File;
import net.aelysium.aelysiummod.config.racas.Deus_Config;
import net.aelysium.aelysiummod.config.racas.Dracono_Config;
import net.aelysium.aelysiummod.config.racas.Elvarin_Config;
import net.aelysium.aelysiummod.config.racas.Humano_Config;
import net.aelysium.aelysiummod.config.racas.Tiefling_Config;
import net.aelysium.aelysiummod.config.racas.Undyne_Config;
import net.aelysium.aelysiummod.config.racas.Valkyria_Config;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public class CarregarConfigs {

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        // Primeira tentativa de carregar todas as configs
        Deus_Config.load(event.getServer());
        Dracono_Config.load(event.getServer());
        Elvarin_Config.load(event.getServer());
        Humano_Config.load(event.getServer());
        Tiefling_Config.load(event.getServer());
        Undyne_Config.load(event.getServer());
        Valkyria_Config.load(event.getServer());

        File configDir = new File(event.getServer().getServerDirectory().toFile(), "config/aelysium");

        // Verifica e cria configs padrão se não existirem
        if (Deus_Config.DATA == null) {
            System.out.println("[Aelysium] Config Deus não existia — criando...");
            Deus_Config.generateDefault(new File(configDir, "deus.json"));
            Deus_Config.load(event.getServer());
        }

        if (Dracono_Config.DATA == null) {
            System.out.println("[Aelysium] Config Dracono não existia — criando...");
            Dracono_Config.generateDefault(new File(configDir, "dracono.json"));
            Dracono_Config.load(event.getServer());
        }

        if (Elvarin_Config.DATA == null) {
            System.out.println("[Aelysium] Config Elvarin não existia — criando...");
            Elvarin_Config.generateDefault(new File(configDir, "elvarin.json"));
            Elvarin_Config.load(event.getServer());
        }

        if (Humano_Config.DATA == null) {
            System.out.println("[Aelysium] Config Humano não existia — criando...");
            Humano_Config.generateDefault(new File(configDir, "humano.json"));
            Humano_Config.load(event.getServer());
        }

        if (Tiefling_Config.DATA == null) {
            System.out.println("[Aelysium] Config Tiefling não existia — criando...");
            Tiefling_Config.generateDefault(new File(configDir, "tiefling.json"));
            Tiefling_Config.load(event.getServer());
        }

        if (Undyne_Config.DATA == null) {
            System.out.println("[Aelysium] Config Undyne não existia — criando...");
            Undyne_Config.generateDefault(new File(configDir, "undyne.json"));
            Undyne_Config.load(event.getServer());
        }

        if (Valkyria_Config.DATA == null) {
            System.out.println("[Aelysium] Config Valkyria não existia — criando...");
            Valkyria_Config.generateDefault(new File(configDir, "valkyria.json"));
            Valkyria_Config.load(event.getServer());
        }
    }
}