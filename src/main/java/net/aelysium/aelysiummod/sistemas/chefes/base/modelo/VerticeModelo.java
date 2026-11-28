package net.aelysium.aelysiummod.sistemas.chefes.base.modelo;

public record VerticeModelo(float x, float y, float z, float u, float v) {

    public VerticeModelo comUv(float u, float v) {
        return new VerticeModelo(x, y, z, u, v);
    }
}
