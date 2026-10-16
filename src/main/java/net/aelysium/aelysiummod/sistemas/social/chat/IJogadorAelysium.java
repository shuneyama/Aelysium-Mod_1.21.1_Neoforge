package net.aelysium.aelysiummod.sistemas.social.chat;

import java.util.List;

public interface IJogadorAelysium {

    void aelysium$createBalloon(String text, int duration, int corTexto, int corFundo, int corBorda, float altura, EstiloBalao estilo);

    List<MensagemBalao> aelysium$getBalloons();

    record MensagemBalao(
            String fullText,
            int corTexto,
            int corFundo,
            int corBorda,
            float altura,
            EstiloBalao estilo,
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
