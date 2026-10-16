package net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;


public final class InicializadorFfmpeg {

    private static String cachedCmd = null;
    private static boolean resolved = false;

    private InicializadorFfmpeg() {}

    public static synchronized String getCommand() {
        if (resolved) return cachedCmd;
        resolved = true;


        Path configDir = Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config/aelysium/ffmpeg");
        String exeName = isWindows() ? "ffmpeg.exe" : "ffmpeg";
        Path localExe = configDir.resolve(exeName);
        if (Files.isRegularFile(localExe)) {
            localExe.toFile().setExecutable(true);
            cachedCmd = localExe.toAbsolutePath().toString();
            return cachedCmd;
        }


        Path doAutoModpack = procurarNoAutoModpack(exeName);
        if (doAutoModpack != null) {
            doAutoModpack.toFile().setExecutable(true);
            cachedCmd = doAutoModpack.toAbsolutePath().toString();
            return cachedCmd;
        }

        if (probeSystemFfmpeg()) {
            cachedCmd = "ffmpeg";
            return cachedCmd;
        }

        return null;
    }

    private static Path procurarNoAutoModpack(String exeName) {
        Path pasta = Minecraft.getInstance().gameDirectory.toPath().resolve("automodpack");
        if (!Files.isDirectory(pasta)) return null;
        try (var stream = Files.find(pasta, 8, (caminho, atributos) -> atributos.isRegularFile()
                && caminho.getFileName().toString().equalsIgnoreCase(exeName))) {
            return stream.max(java.util.Comparator.comparingLong(caminho -> caminho.toFile().lastModified())).orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    public static void resetCache() {
        resolved = false;
        cachedCmd = null;
    }

    private static boolean probeSystemFfmpeg() {
        try {
            Process p = new ProcessBuilder("ffmpeg", "-version")
                    .redirectErrorStream(true)
                    .start();
            try (InputStream is = p.getInputStream()) {
                is.transferTo(OutputStream.nullOutputStream());
            }
            p.waitFor();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
