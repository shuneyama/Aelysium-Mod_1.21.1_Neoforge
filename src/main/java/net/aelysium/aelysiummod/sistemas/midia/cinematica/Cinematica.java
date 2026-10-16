package net.aelysium.aelysiummod.sistemas.midia.cinematica;

public record Cinematica(
        String nome,
        int fadeInTicks,
        int fadeOutTicks,
        long startTimeMillis
) {
}
