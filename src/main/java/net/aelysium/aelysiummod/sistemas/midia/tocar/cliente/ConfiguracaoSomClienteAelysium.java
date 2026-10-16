package net.aelysium.aelysiummod.sistemas.midia.tocar.cliente;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.aelysium.aelysiummod.nucleo.configuracao.AelysiumDiretorioConfiguracao;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@OnlyIn(Dist.CLIENT)
public class ConfiguracaoSomClienteAelysium {

    private static final Path ARQUIVO = AelysiumDiretorioConfiguracao.file("som.toml");

    private static double volumeNormalizado = 1.0;

    public static void carregar() {
        if (!Files.exists(ARQUIVO)) {
            salvar();
            return;
        }
        try {
            CommentedFileConfig cfg = CommentedFileConfig.builder(ARQUIVO).charset(StandardCharsets.UTF_8).build();
            cfg.load();
            double v = cfg.<Number>getOrElse("volume", 1.0).doubleValue();
            volumeNormalizado = Math.max(0.0, Math.min(1.0, v));
            cfg.close();
        } catch (Exception ignored) {}
    }

    public static void salvar() {
        try {
            Files.createDirectories(ARQUIVO.getParent());
            String conteudo = "# Volume dos sons do servidor (0.0 = mudo, 1.0 = máximo)\n"
                    + "volume = " + volumeNormalizado + "\n";
            Files.writeString(ARQUIVO, conteudo, StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    public static double getVolumeNormalizado() {
        return volumeNormalizado;
    }

    public static void setVolumeNormalizado(double valor) {
        volumeNormalizado = Math.max(0.0, Math.min(1.0, valor));
        salvar();
    }
}
