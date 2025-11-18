package net.aelysium.aelysiummod.util;

import net.aelysium.aelysiummod.command.racas.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public class ServidorEventos {

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        // Carrega todas as configs primeiro
        Deus_Config.load(event.getServer());
        Dracono_Config.load(event.getServer());
        Elvarin_Config.load(event.getServer());
        Humano_Config.load(event.getServer());
        Tiefling_Config.load(event.getServer());
        Undyne_Config.load(event.getServer());
        Valkyria_Config.load(event.getServer());

        // Depois verifica se alguma é null e cria
        if (Deus_Config.DATA == null) {
            System.out.println("[Aelysium] Config Deus não existia — criando...");
            Deus_Config.generateDefault(new java.io.File(
                    event.getServer().getServerDirectory().toFile(),
                    "config/aelysium/deus.json"
            ));
            Deus_Config.load(event.getServer());
        }

        if (Dracono_Config.DATA == null) {
            System.out.println("[Aelysium] Config Dracono não existia — criando...");
            Dracono_Config.generateDefault(new java.io.File(
                    event.getServer().getServerDirectory().toFile(),
                    "config/aelysium/dracono.json"
            ));
            Dracono_Config.load(event.getServer());
        }

        if (Elvarin_Config.DATA == null) {
            System.out.println("[Aelysium] Config Elvarin não existia — criando...");
            Elvarin_Config.generateDefault(new java.io.File(
                    event.getServer().getServerDirectory().toFile(),
                    "config/aelysium/elvarin.json"
            ));
            Elvarin_Config.load(event.getServer());
        }

        if (Humano_Config.DATA == null) {
            System.out.println("[Aelysium] Config Humano não existia — criando...");
            Humano_Config.generateDefault(new java.io.File(
                    event.getServer().getServerDirectory().toFile(),
                    "config/aelysium/humano.json"
            ));
            Humano_Config.load(event.getServer());
        }

        if (Tiefling_Config.DATA == null) {
            System.out.println("[Aelysium] Config Tiefling não existia — criando...");
            Tiefling_Config.generateDefault(new java.io.File(
                    event.getServer().getServerDirectory().toFile(),
                    "config/aelysium/tiefling.json"
            ));
            Tiefling_Config.load(event.getServer());
        }

        if (Undyne_Config.DATA == null) {
            System.out.println("[Aelysium] Config Undyne não existia — criando...");
            Undyne_Config.generateDefault(new java.io.File(
                    event.getServer().getServerDirectory().toFile(),
                    "config/aelysium/undyne.json"
            ));
            Undyne_Config.load(event.getServer());
        }

        if (Valkyria_Config.DATA == null) {
            System.out.println("[Aelysium] Config Valkyria não existia — criando...");
            Valkyria_Config.generateDefault(new java.io.File(
                    event.getServer().getServerDirectory().toFile(),
                    "config/aelysium/valkyria.json"
            ));
            Valkyria_Config.load(event.getServer());
        }
    }
}