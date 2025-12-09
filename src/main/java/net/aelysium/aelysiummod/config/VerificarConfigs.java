package net.aelysium.aelysiummod.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraft.server.MinecraftServer;

public class VerificarConfigs {

    public static File getConfigFile(MinecraftServer server, String fileName) {
        Path[] possiblePaths = {
                server.getServerDirectory().resolve("config/aelysium/" + fileName),
                Paths.get("config/aelysium/" + fileName),
                Paths.get(System.getProperty("user.dir"), "config/aelysium/" + fileName)
        };

        for (Path path : possiblePaths) {
            File file = path.toFile();
            if (file.exists()) {
                System.out.println("[Aelysium] Config encontrada em: " + file.getAbsolutePath());
                return file;
            }
        }

        // Se não encontrou, usa o primeiro caminho como padrão
        File file = possiblePaths[0].toFile();
        System.out.println("[Aelysium] Usando caminho de config: " + file.getAbsolutePath());
        return file;
    }

    public static boolean ensureConfigDirectory(File configFile) {
        File parentDir = configFile.getParentFile();
        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            System.out.println("[Aelysium] Criando diretório: " + parentDir.getAbsolutePath() + " - Sucesso: " + created);
            return created;
        }
        return true;
    }
}