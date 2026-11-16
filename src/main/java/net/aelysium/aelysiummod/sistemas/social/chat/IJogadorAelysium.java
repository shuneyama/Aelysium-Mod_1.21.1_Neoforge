package net.aelysium.aelysiummod.sistemas.social.chat;

import net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo;
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

            if (visibleChars == 0) return "";
            return CodigosEstilo.prefixoVisivel(fullText, visibleChars);
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
