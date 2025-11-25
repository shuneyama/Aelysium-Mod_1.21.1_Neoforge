package net.aelysium.aelysiummod.util;

import net.minecraft.server.MinecraftServer;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VerificarConfigs {

    /**
     * Retorna o caminho correto da config independente do ambiente
     */
    public static File getConfigFile(MinecraftServer server, String fileName) {
        // Tenta vários caminhos possíveis
        Path[] possiblePaths = {
                // Caminho 1: Relativo ao diretório do servidor
                server.getServerDirectory().resolve("config/aelysium/" + fileName),

                // Caminho 2: Absoluto na pasta do jogo
                Paths.get("config/aelysium/" + fileName),

                // Caminho 3: No diretório de trabalho atual
                Paths.get(System.getProperty("user.dir"), "config/aelysium/" + fileName)
        };

        // Verifica qual caminho já tem arquivo
        for (Path path : possiblePaths) {
            File file = path.toFile();
            if (file.exists()) {
                System.out.println("[Aelysium] Config encontrada em: " + file.getAbsolutePath());
                return file;
            }
        }

        // Se nenhum existir, usa o primeiro caminho e cria
        File file = possiblePaths[0].toFile();
        System.out.println("[Aelysium] Usando caminho de config: " + file.getAbsolutePath());
        return file;
    }

    /**
     * Garante que o diretório de configs existe
     */
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