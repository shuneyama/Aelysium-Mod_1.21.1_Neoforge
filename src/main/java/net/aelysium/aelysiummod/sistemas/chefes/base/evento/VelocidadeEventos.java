package net.aelysium.aelysiummod.sistemas.chefes.base.evento;

import java.util.function.IntSupplier;

public final class VelocidadeEventos {
    private static final ThreadLocal<double[]> ATUAL = ThreadLocal.withInitial(() -> new double[]{1.0});

    private VelocidadeEventos() {
    }

    public static double atual() {
        return ATUAL.get()[0];
    }

    public static void executar(double velocidade, Runnable acao) {
        double[] atual = ATUAL.get();
        double anterior = atual[0];
        atual[0] = velocidade > 0 ? velocidade : 1.0;
        try {
            acao.run();
        } finally {
            atual[0] = anterior;
        }
    }

    public static int executar(double velocidade, IntSupplier acao) {
        double[] atual = ATUAL.get();
        double anterior = atual[0];
        atual[0] = velocidade > 0 ? velocidade : 1.0;
        try {
            return acao.getAsInt();
        } finally {
            atual[0] = anterior;
        }
    }
}
