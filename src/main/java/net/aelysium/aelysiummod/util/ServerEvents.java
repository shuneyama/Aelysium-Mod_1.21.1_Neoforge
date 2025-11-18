package net.aelysium.aelysiummod.util;

import net.aelysium.aelysiummod.command.racas.Deus_Config;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public class ServerEvents {

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        Deus_Config.load(event.getServer());

        if (Deus_Config.DATA == null) {
            System.out.println("[Aelysium] Config não existia — criando...");
            Deus_Config.generateDefault(new java.io.File(
                    event.getServer().getServerDirectory().toFile(),
                    "config/aelysium/deus.json"
            ));
            Deus_Config.load(event.getServer());
        }
    }
}
