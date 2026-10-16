package net.aelysium.aelysiummod.sistemas.midia.cinematica;

public final class ConfiguracaoTransferenciaVideo {

    public static final int CHUNK_SIZE = 512 * 1024;
    public static final long TAMANHO_MAXIMO_BYTES = 200L * 1024L * 1024L;
    public static final long INTERVALO_CHUNK_MS = 100L;

    private ConfiguracaoTransferenciaVideo() {}
}
