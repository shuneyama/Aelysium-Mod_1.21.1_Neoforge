package net.aelysium.aelysiummod.nucleo.armazenamento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MigracaoPasta {

    private static final Logger LOGGER = LoggerFactory.getLogger("AelysiumArmazenamento");

    public static void moverPasta(Path antiga, Path nova) {
        if (!Files.isDirectory(antiga)) return;
        try {
            Files.createDirectories(nova);
            try (var stream = Files.list(antiga)) {
                for (Path item : stream.toList()) {
                    Path alvo = nova.resolve(item.getFileName().toString());
                    if (!Files.exists(alvo)) Files.move(item, alvo);
                }
            }
            try {
                Files.deleteIfExists(antiga);
            } catch (IOException ignored) {}
            LOGGER.info("Pasta {} movida para {}.", antiga, nova);
        } catch (IOException e) {
            LOGGER.error("Erro ao mover {} para {}: ", antiga, nova, e);
        }
    }

    public static void moverArquivo(Path antigo, Path novo) {
        if (!Files.isRegularFile(antigo) || Files.exists(novo)) return;
        try {
            Files.createDirectories(novo.getParent());
            Files.move(antigo, novo);
            LOGGER.info("Arquivo {} movido para {}.", antigo, novo);
        } catch (IOException e) {
            LOGGER.error("Erro ao mover {} para {}: ", antigo, novo, e);
        }
    }
}
