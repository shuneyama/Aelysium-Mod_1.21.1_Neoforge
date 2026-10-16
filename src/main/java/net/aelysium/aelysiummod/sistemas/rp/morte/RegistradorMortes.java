package net.aelysium.aelysiummod.sistemas.rp.morte;

import net.aelysium.aelysiummod.sistemas.admin.historico.FormatoDataHistorico;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class RegistradorMortes {

    private static final Logger LOGGER = LoggerFactory.getLogger("Aelysium-MorteCanonica");
    private static final String NOME_ARQUIVO = "mortes_canonicas.txt";
    private static boolean migrado = false;

    public static void registrar(ServerPlayer player, DamageSource source, boolean duranteEvento) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        String nick = player.getName().getString();
        String causa = source.getLocalizedDeathMessage(player).getString();
        String horario = FormatoDataHistorico.agora();
        String local = String.format("X=%d, Y=%d, Z=%d, Dimensão=%s",
                player.getBlockX(), player.getBlockY(), player.getBlockZ(),
                player.level().dimension().location());
        String situacao = duranteEvento ? "DURANTE O EVENTO" : "FORA DO EVENTO, COM O EFEITO DE MORTE";

        String linha = "[" + horario + "] [" + situacao + "] " + nick + " morreu (" + causa + ") em " + local;

        try {
            Path raiz = server.getWorldPath(LevelResource.ROOT);
            Path arquivo = raiz.resolve("aelysium").resolve(NOME_ARQUIVO);
            migrarArquivosAntigos(raiz, arquivo);
            Files.createDirectories(arquivo.getParent());
            Files.writeString(arquivo, linha + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.error("Falha ao registrar morte canônica de {}", nick, e);
        }
    }

    private static synchronized void migrarArquivosAntigos(Path raiz, Path destino) throws IOException {
        if (migrado) return;
        migrado = true;

        Path pastaAelysium = raiz.resolve("aelysium");
        Path[] pastasAntigas = {pastaAelysium.resolve("morte_canonica"), raiz.resolve("aelysium_morte_canonica")};
        for (Path pasta : pastasAntigas) {
            Path antigo = pasta.resolve(NOME_ARQUIVO);
            if (Files.isRegularFile(antigo)) {
                Files.createDirectories(destino.getParent());
                Files.writeString(destino, Files.readString(antigo, StandardCharsets.UTF_8),
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                Files.delete(antigo);
            }
            try {
                Files.deleteIfExists(pasta);
            } catch (IOException ignored) {}
        }
    }
}
