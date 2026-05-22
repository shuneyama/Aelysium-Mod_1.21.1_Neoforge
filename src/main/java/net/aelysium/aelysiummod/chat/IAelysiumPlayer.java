package net.aelysium.aelysiummod.chat;

import java.util.List;

public interface IAelysiumPlayer {

    void aelysium$createBalloon(String text, int duration, int corTexto, int corFundo, int corBorda, float altura, BalloonStyle estilo);

    List<BalloonMessage> aelysium$getBalloons();

    record BalloonMessage(
            String fullText,
            int corTexto,
            int corFundo,
            int corBorda,
            float altura,
            BalloonStyle estilo,
            long startTime,
            int[] charDelays
    ) {
        public String getVisibleText(long currentTime) {
            long elapsed = currentTime - startTime;
            int visibleChars = 0;
            long accumulatedDelay = 0;

            for (int i = 0; i < charDelays.length; i++) {
                accumulatedDelay += charDelays[i];
                if (elapsed >= accumulatedDelay) {
                    visibleChars = i + 1;
                } else {
                    break;
                }
            }

            return fullText.substring(0, Math.min(visibleChars, fullText.length()));
        }

        public boolean isFullyRevealed(long currentTime) {
            long totalDelay = 0;
            for (int delay : charDelays) {
                totalDelay += delay;
            }
            return (currentTime - startTime) >= totalDelay;
        }
    }
}
