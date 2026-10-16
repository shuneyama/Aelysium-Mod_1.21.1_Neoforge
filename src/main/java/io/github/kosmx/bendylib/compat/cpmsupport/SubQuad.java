package io.github.kosmx.bendylib.compat.cpmsupport;

public final class SubQuad {
    public final TempPos[] points;
    public final float[] u;
    public final float[] v;

    public SubQuad(TempPos[] points, float[] u, float[] v) {
        this.points = points;
        this.u = u;
        this.v = v;
    }
}
