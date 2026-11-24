package net.aelysium.aelysiummod.sistemas.rp;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PastaPersonagens {
    private PastaPersonagens() {
    }

    public static Path arquivo(MinecraftServer servidor, String nome) {
        Path base = servidor.getWorldPath(LevelResource.ROOT).resolve("aelysium");
        Path novo = base.resolve("personagens").resolve(nome);
        Path antigo = base.resolve(nome);
        try {
            Files.createDirectories(novo.getParent());
            if (!Files.exists(novo) && Files.exists(antigo)) Files.move(antigo, novo);
        } catch (IOException ignorado) {
        }
        return novo;
    }
}
