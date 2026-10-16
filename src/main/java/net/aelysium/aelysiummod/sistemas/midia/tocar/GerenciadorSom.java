package net.aelysium.aelysiummod.sistemas.midia.tocar;

import net.aelysium.aelysiummod.nucleo.armazenamento.MigracaoPasta;
import net.aelysium.aelysiummod.nucleo.armazenamento.PastaAelysium;
import net.aelysium.aelysiummod.nucleo.configuracao.AelysiumDiretorioConfiguracao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorSom {

    public static void inicializar() {
        MigracaoPasta.moverPasta(AelysiumDiretorioConfiguracao.get().resolve("sons"), getPastaSons());
        try {
            Files.createDirectories(getPastaSons());
        } catch (IOException ignored) {}
    }

    public static Path getPastaSons() {
        return PastaAelysium.pasta("sons");
    }

    public static List<String> listarSons() {
        List<String> nomes = new ArrayList<>();
        if (!PastaAelysium.pronta() || !Files.exists(getPastaSons())) return nomes;
        try (var stream = Files.list(getPastaSons())) {
            stream.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".ogg"))
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .forEach(nomes::add);
        } catch (IOException ignored) {}
        return nomes;
    }

    public static Path getArquivoSom(String nome) {
        String nomeSanitizado = nome.replaceAll("[/\\\\]", "");
        return getPastaSons().resolve(nomeSanitizado);
    }

    public static boolean somExiste(String nome) {
        if (!PastaAelysium.pronta() || !nome.toLowerCase().endsWith(".ogg")) return false;
        Path arquivo = getArquivoSom(nome);
        return Files.exists(arquivo) && Files.isRegularFile(arquivo);
    }
}
