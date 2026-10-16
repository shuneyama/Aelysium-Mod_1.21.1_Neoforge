package net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class SondaMidia {

    private static final Pattern RES      = Pattern.compile("(\\d{2,5})x(\\d{2,5})");
    private static final Pattern FPS      = Pattern.compile("([\\d.]+)\\s*fps");
    private static final Pattern TBR      = Pattern.compile("([\\d.]+)\\s*tbr");
    private static final Pattern DURATION = Pattern.compile("Duration:\\s+(\\d+):(\\d+):([\\d.]+)");

    private SondaMidia() {}

    public static Informacoes probe(String ffmpegCmd, Path videoFile) throws IOException {

        Process p = new ProcessBuilder(ffmpegCmd, "-i", videoFile.toAbsolutePath().toString())
                .redirectErrorStream(true)
                .start();

        StringBuilder out = new StringBuilder();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) out.append(line).append('\n');
        }
        try { p.waitFor(); } catch (InterruptedException ignored) {}

        String text = out.toString();

        Matcher rm = RES.matcher(text);
        if (!rm.find()) throw new IOException("Resolução não detectada: " + videoFile.getFileName());
        int width  = Integer.parseInt(rm.group(1));
        int height = Integer.parseInt(rm.group(2));


        double fps = 30.0;
        Matcher fm = FPS.matcher(text);
        if (fm.find()) {
            fps = Double.parseDouble(fm.group(1));
        } else {
            Matcher tm = TBR.matcher(text);
            if (tm.find()) fps = Double.parseDouble(tm.group(1));
        }
        if (fps <= 0 || fps > 300) fps = 30.0;

        double duration = 0.0;
        Matcher dm = DURATION.matcher(text);
        if (dm.find()) {
            int h = Integer.parseInt(dm.group(1));
            int m = Integer.parseInt(dm.group(2));
            double s = Double.parseDouble(dm.group(3));
            duration = h * 3600.0 + m * 60.0 + s;
        }

        return new Informacoes(width, height, fps, duration);
    }

    public static final class Informacoes {
        public final int width;
        public final int height;
        public final double fps;
        public final double durationSeconds;

        public Informacoes(int width, int height, double fps, double durationSeconds) {
            this.width = width;
            this.height = height;
            this.fps = fps;
            this.durationSeconds = durationSeconds;
        }
    }
}
