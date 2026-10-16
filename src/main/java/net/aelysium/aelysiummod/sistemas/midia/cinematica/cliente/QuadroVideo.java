package net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente;

public final class QuadroVideo {
    public final int width;
    public final int height;
    public final byte[] rgba;

    public QuadroVideo(int width, int height, byte[] rgba) {
        this.width = width;
        this.height = height;
        this.rgba = rgba;
    }
}
