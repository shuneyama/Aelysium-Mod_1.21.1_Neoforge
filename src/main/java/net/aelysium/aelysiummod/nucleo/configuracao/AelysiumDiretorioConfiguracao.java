package net.aelysium.aelysiummod.nucleo.configuracao;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AelysiumDiretorioConfiguracao {

    private static Path cached = null;

    public static Path get() {
        if (cached == null) {
            cached = FMLPaths.CONFIGDIR.get().resolve("aelysium");
            try {
                Files.createDirectories(cached);
            } catch (IOException ignored) {}
        }
        return cached;
    }

    public static Path file(String filename) {
        return get().resolve(filename);
    }
}