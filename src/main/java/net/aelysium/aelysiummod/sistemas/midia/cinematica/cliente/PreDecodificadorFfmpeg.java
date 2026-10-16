package net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class PreDecodificadorFfmpeg {

    private static final Pattern FRAME_PAT = Pattern.compile("frame=\\s*(\\d+)");

    private volatile float  progress   = 0f;
    private volatile boolean done      = false;
    private volatile boolean failed    = false;
    private volatile String  failReason = "";

    private Process       process;
    private volatile Path outputFile;


    public void start(String ffmpegCmd, Path videoFile, int width, int height, int totalFrames) throws IOException {
        outputFile = Files.createTempFile("aelysium_cutscene_", ".raw");
        outputFile.toFile().deleteOnExit();

        process = new ProcessBuilder(
                ffmpegCmd,
                "-y",
                "-i", videoFile.toAbsolutePath().toString(),
                "-an",
                "-vf", "scale=" + width + ":" + height,
                "-r", "30",
                "-f", "rawvideo", "-pix_fmt", "rgba",
                outputFile.toAbsolutePath().toString()
        ).start();


        Thread stdoutDrain = new Thread(
                () -> { try { process.getInputStream().transferTo(OutputStream.nullOutputStream()); } catch (IOException ignored) {} },
                "predecoder-stdout-drain"
        );
        stdoutDrain.setDaemon(true);
        stdoutDrain.start();

        Thread monitor = new Thread(() -> monitorProgress(totalFrames), "predecoder-monitor");
        monitor.setDaemon(true);
        monitor.start();
    }

    private void monitorProgress(int totalFrames) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = FRAME_PAT.matcher(line);
                if (m.find() && totalFrames > 0) {
                    int frame = Integer.parseInt(m.group(1));
                    progress = Math.min(0.99f, (float) frame / totalFrames);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                progress = 1f;
                done = true;
            } else {
                failReason = "ffmpeg saiu com código " + exitCode;
                failed = true;
            }
        } catch (Exception e) {
            failReason = e.getMessage() != null ? e.getMessage() : "erro desconhecido";
            failed = true;
        }
    }

    public float   getProgress()  { return progress; }
    public boolean isDone()       { return done; }
    public boolean isFailed()     { return failed; }
    public String  getFailReason(){ return failReason; }


    public Path takeOutputFile() {
        Path f = outputFile;
        outputFile = null;
        return f;
    }


    public void close() {
        if (process != null) process.destroyForcibly();
        if (outputFile != null) {
            try { Files.deleteIfExists(outputFile); } catch (IOException ignored) {}
            outputFile = null;
        }
    }
}
