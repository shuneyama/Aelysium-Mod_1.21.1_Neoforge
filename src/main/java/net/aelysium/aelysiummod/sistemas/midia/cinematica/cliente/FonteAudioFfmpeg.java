package net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;


public final class FonteAudioFfmpeg implements AutoCloseable {

    private static final int SAMPLE_RATE = 48000;
    private static final int CHANNELS    = 2;

    private final String ffmpegCmd;

    private Process process;
    private SourceDataLine line;
    private Thread pumpThread;
    private volatile boolean running = false;

    public FonteAudioFfmpeg(String ffmpegCmd) {
        this.ffmpegCmd = ffmpegCmd;
    }


    public boolean prepare(Path videoFile) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, CHANNELS, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) return false;

            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);


            process = new ProcessBuilder(
                    ffmpegCmd,
                    "-i", videoFile.toAbsolutePath().toString(),
                    "-vn",
                    "-f", "s16le",
                    "-ar", String.valueOf(SAMPLE_RATE),
                    "-ac", String.valueOf(CHANNELS),
                    "-"
            ).start();
            running = true;

            Thread stderrDrain = new Thread(() -> drainQuietly(process.getErrorStream()), "ffmpeg-audio-stderr");
            stderrDrain.setDaemon(true);
            stderrDrain.start();

            pumpThread = new Thread(this::pump, "ffmpeg-audio-pump");
            pumpThread.setDaemon(true);
            pumpThread.start();
            return true;
        } catch (LineUnavailableException | IOException e) {
            close();
            return false;
        }
    }


    public void beginPlayback() {
        if (line != null) line.start();
    }

    private void pump() {
        byte[] buf = new byte[8192];
        InputStream stdout = process.getInputStream();
        try {
            int n;
            while (running && (n = stdout.read(buf)) != -1) {
                line.write(buf, 0, n);
            }
        } catch (IOException ignored) {
        }
    }

    public boolean isPlaying() {
        return process != null && process.isAlive();
    }

    private static void drainQuietly(InputStream s) {
        try { s.transferTo(java.io.OutputStream.nullOutputStream()); } catch (IOException ignored) {}
    }

    @Override
    public void close() {
        running = false;
        if (process != null) process.destroyForcibly();
        if (line != null) {
            try { line.stop(); } catch (Exception ignored) {}
        }
        if (pumpThread != null) {
            try { pumpThread.join(500); } catch (InterruptedException ignored) {}
        }
        if (line != null) {
            try { line.close(); } catch (Exception ignored) {}
            line = null;
        }
    }
}
