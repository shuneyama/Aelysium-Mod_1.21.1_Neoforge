package net.aelysium.aelysiummod.compatibilidade.articulacoes.cuboide;

public final class DadosCuboide {

    public final float x;
    public final float y;
    public final float z;
    public final float tamanhoX;
    public final float tamanhoY;
    public final float tamanhoZ;
    public final float extraX;
    public final float extraY;
    public final float extraZ;
    public final int u;
    public final int v;
    public final boolean espelhado;
    public final int larguraTextura;
    public final int alturaTextura;
    public int pivo = -1;

    public DadosCuboide(int u, int v, float x, float y, float z, float tamanhoX, float tamanhoY, float tamanhoZ, float extraX, float extraY, float extraZ, boolean espelhado, float larguraTextura, float alturaTextura) {
        this.u = u;
        this.v = v;
        this.x = x;
        this.y = y;
        this.z = z;
        this.tamanhoX = tamanhoX;
        this.tamanhoY = tamanhoY;
        this.tamanhoZ = tamanhoZ;
        this.extraX = extraX;
        this.extraY = extraY;
        this.extraZ = extraZ;
        this.espelhado = espelhado;
        this.larguraTextura = (int) larguraTextura;
        this.alturaTextura = (int) alturaTextura;
    }
}
