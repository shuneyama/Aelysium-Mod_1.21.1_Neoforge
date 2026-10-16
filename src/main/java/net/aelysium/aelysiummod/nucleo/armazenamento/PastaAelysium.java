package net.aelysium.aelysiummod.nucleo.armazenamento;

import net.aelysium.aelysiummod.nucleo.configuracao.AelysiumDiretorioConfiguracao;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;

public class PastaAelysium {

    private static MinecraftServer servidor;

    public static void inicializar(MinecraftServer server) {
        servidor = server;
    }

    public static MinecraftServer servidor() {
        return servidor;
    }

    public static boolean pronta() {
        return servidor != null;
    }

    public static Path raiz() {
        return servidor.getWorldPath(LevelResource.ROOT).resolve("aelysium");
    }

    public static Path pasta(String nome) {
        return raiz().resolve(nome);
    }

    public static Path arquivo(String nome) {
        return raiz().resolve(nome);
    }

    public static Path arquivoMigrado(String nomeNovo, String nomeAntigoNaConfig) {
        Path antigo = AelysiumDiretorioConfiguracao.file(nomeAntigoNaConfig);
        if (servidor == null) return antigo;
        Path novo = raiz().resolve(nomeNovo);
        if (!Files.exists(novo)) MigracaoPasta.moverArquivo(antigo, novo);
        return novo;
    }
}
