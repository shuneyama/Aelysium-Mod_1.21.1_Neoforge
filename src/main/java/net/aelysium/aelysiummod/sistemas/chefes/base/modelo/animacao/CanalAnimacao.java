package net.aelysium.aelysiummod.sistemas.chefes.base.modelo.animacao;

public record CanalAnimacao(QuadroChave[] x, QuadroChave[] y, QuadroChave[] z) {

    public float x(double tick) {
        return (float) avaliar(x, tick);
    }

    public float y(double tick) {
        return (float) avaliar(y, tick);
    }

    public float z(double tick) {
        return (float) avaliar(z, tick);
    }

    public double ultimoTempo() {
        double total = 0;
        for (QuadroChave quadro : x) total += quadro.duracao();
        return total;
    }

    private static double avaliar(QuadroChave[] quadros, double tick) {
        double total = 0;
        for (QuadroChave quadro : quadros) {
            total += quadro.duracao();
            if (total > tick) return quadro.valor(tick - (total - quadro.duracao()));
        }
        return quadros[quadros.length - 1].valor(tick);
    }
}
