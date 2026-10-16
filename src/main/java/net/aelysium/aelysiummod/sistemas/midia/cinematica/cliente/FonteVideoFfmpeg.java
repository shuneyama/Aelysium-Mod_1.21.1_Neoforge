package net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;


public final class FonteVideoFfmpeg implements AutoCloseable {

    private final FilaQuadros frameQueue;

    private Process process;
    private Thread  readerThread;
    private volatile boolean running = false;

    public FonteVideoFfmpeg(FilaQuadros frameQueue) {
        this.frameQueue = frameQueue;
    }


    public void start(String ffmpegCmd, Path videoFile, int width, int height) throws IOException {
        running = true;
        process = new ProcessBuilder(
                ffmpegCmd,
                "-i", videoFile.toAbsolutePath().toString(),
                "-an",
                "-vf", "scale=" + width + ":" + height,
                "-r", "30",
                "-f", "rawvideo",
                "-pix_fmt", "rgba",
                "-"
        ).start();

        Thread stderrDrain = new Thread(() -> drainQuietly(process.getErrorStream()), "ffmpeg-video-stderr");
        stderrDrain.setDaemon(true);
        stderrDrain.start();

        readerThread = new Thread(
                () -> readFrames(process.getInputStream(), width, height),
                "ffmpeg-video-reader"
        );
        readerThread.setDaemon(true);
        readerThread.start();
    }


    public void startFromFile(Path rawFile, int width, int height) {
        running = true;
        readerThread = new Thread(
                () -> {
                    try (FileInputStream fis = new FileInputStream(rawFile.toFile())) {
                        readFrames(fis, width, height);
                    } catch (IOException ignored) {}
                },
                "video-frame-reader"
        );
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void readFrames(InputStream src, int width, int height) {
        int frameBytes = width * height * 4;

        byte[][] pool = new byte[8][frameBytes];
        int poolIdx   = 0;

        try {
            while (running) {
                byte[] rgba = pool[poolIdx & 7];
                poolIdx++;

                int offset = 0;
                while (offset < frameBytes) {
                    int n = src.read(rgba, offset, frameBytes - offset);
                    if (n == -1) return;
                    offset += n;
                }

                if (!frameQueue.offer(new QuadroVideo(width, height, rgba))) return;
            }
        } catch (IOException | InterruptedException ignored) {}
    }

    public boolean isPlaying() {
        if (process != null) return process.isAlive();
        return readerThread != null && readerThread.isAlive();
    }

    private static void drainQuietly(InputStream s) {
        try { s.transferTo(java.io.OutputStream.nullOutputStream()); } catch (IOException ignored) {}
    }

    @Override
    public void close() {
        running = false;
        if (readerThread != null) readerThread.interrupt();
        if (process != null) process.destroyForcibly();
    }
}
