package net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente;

import java.io.IOException;
import java.nio.file.Path;


public final class ReprodutorMidia implements AutoCloseable {

    private final FilaQuadros frameQueue = new FilaQuadros(4);

    private FonteVideoFfmpeg videoSource;
    private FonteAudioFfmpeg audioSource;
    private boolean audioStarted = false;

    private double frameDuration;
    private long   framesShown = 0;
    private QuadroVideo lastFrame = null;

    private long startNanos = 0;


    public void playFromPreDecoded(String ffmpegCmd, Path videoFile, Path preDecodedFile,
                                   int width, int height, double sourceFps) throws IOException {
        frameDuration = 1.0 / Math.min(sourceFps, 30.0);

        audioSource = new FonteAudioFfmpeg(ffmpegCmd);
        if (!audioSource.prepare(videoFile)) {
            audioSource.close();
            audioSource = null;
        }

        videoSource = new FonteVideoFfmpeg(frameQueue);
        videoSource.startFromFile(preDecodedFile, width, height);

        if (audioSource != null) audioSource.beginPlayback();
        startNanos   = System.nanoTime();
        audioStarted = true;
    }


    public void play(String ffmpegCmd, Path videoFile) throws IOException {
        SondaMidia.Informacoes info = SondaMidia.probe(ffmpegCmd, videoFile);
        frameDuration = 1.0 / Math.min(info.fps, 30.0);

        int outWidth  = info.width;
        int outHeight = info.height;
        if (outHeight > 720) {
            outWidth  = (info.width * 720 / info.height) & ~1;
            outHeight = 720;
        }

        audioSource = new FonteAudioFfmpeg(ffmpegCmd);
        if (!audioSource.prepare(videoFile)) {
            audioSource.close();
            audioSource = null;
        }

        videoSource = new FonteVideoFfmpeg(frameQueue);
        videoSource.start(ffmpegCmd, videoFile, outWidth, outHeight);

        if (audioSource != null) audioSource.beginPlayback();
        startNanos   = System.nanoTime();
        audioStarted = true;
    }


    public QuadroVideo currentFrame() {
        if (startNanos == 0) return lastFrame;

        double clock = (System.nanoTime() - startNanos) / 1_000_000_000.0;

        QuadroVideo peeked;
        while ((peeked = frameQueue.peek()) != null) {
            double frameTime = framesShown * frameDuration;
            if (frameTime > clock) break;
            lastFrame = frameQueue.poll();
            framesShown++;
        }
        return lastFrame;
    }

    public boolean isFinished() {
        return lastFrame != null
            && videoSource != null
            && !videoSource.isPlaying()
            && frameQueue.isEmpty();
    }

    @Override
    public void close() {
        if (videoSource != null) { videoSource.close(); videoSource = null; }
        if (audioSource != null) { audioSource.close(); audioSource = null; }
        frameQueue.clear();
        lastFrame    = null;
        framesShown  = 0;
        startNanos   = 0;
        audioStarted = false;
    }
}
