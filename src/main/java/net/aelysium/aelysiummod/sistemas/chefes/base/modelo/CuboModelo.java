package net.aelysium.aelysiummod.sistemas.chefes.base.modelo;

public record CuboModelo(QuadModelo[] quads, float pivoX, float pivoY, float pivoZ, float rotX, float rotY, float rotZ, float largura, float altura, float profundidade) {

    public boolean temRotacao() {
        return rotX != 0 || rotY != 0 || rotZ != 0;
    }
}
