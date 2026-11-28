package net.aelysium.aelysiummod.sistemas.chefes.base.modelo.animacao;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

public final class Suavizacao {
    private static final Map<String, Suavizacao> POR_NOME = new HashMap<>();

    public static final Suavizacao LINEAR = registrar(new Suavizacao(arg -> t -> t, false), "linear", "none");
    public static final Suavizacao CATMULLROM = registrar(new Suavizacao(arg -> entradaSaida(Suavizacao::catmullRom), true), "catmullrom");

    static {
        registrar(new Suavizacao(Suavizacao::degrau, false), "step");
        familia("sine", arg -> Suavizacao::seno);
        familia("quad", arg -> t -> t * t);
        familia("cubic", arg -> t -> t * t * t);
        familia("quart", arg -> t -> Math.pow(t, 4));
        familia("quint", arg -> t -> Math.pow(t, 5));
        familia("expo", arg -> t -> Math.pow(2, 10 * (t - 1)));
        familia("circ", arg -> t -> 1 - Math.sqrt(1 - t * t));
        familia("back", Suavizacao::volta);
        familia("elastic", Suavizacao::elastico);
        familia("bounce", Suavizacao::quique);
    }

    private final Function<Double, DoubleUnaryOperator> fabrica;
    private final boolean spline;

    private Suavizacao(Function<Double, DoubleUnaryOperator> fabrica, boolean spline) {
        this.fabrica = fabrica;
        this.spline = spline;
    }

    public static Suavizacao porNome(String nome) {
        return POR_NOME.getOrDefault(nome.toLowerCase(Locale.ROOT), LINEAR);
    }

    public boolean isSpline() {
        return spline;
    }

    public DoubleUnaryOperator curva(Double argumento) {
        return fabrica.apply(argumento);
    }

    private static Suavizacao registrar(Suavizacao suavizacao, String... nomes) {
        for (String nome : nomes) POR_NOME.putIfAbsent(nome, suavizacao);
        return suavizacao;
    }

    private static void familia(String nome, Function<Double, DoubleUnaryOperator> base) {
        registrar(new Suavizacao(base, false), "easein" + nome);
        registrar(new Suavizacao(arg -> saida(base.apply(arg)), false), "easeout" + nome);
        registrar(new Suavizacao(arg -> entradaSaida(base.apply(arg)), false), "easeinout" + nome);
    }

    private static DoubleUnaryOperator saida(DoubleUnaryOperator funcao) {
        return t -> 1 - funcao.applyAsDouble(1 - t);
    }

    private static DoubleUnaryOperator entradaSaida(DoubleUnaryOperator funcao) {
        return t -> t < 0.5d ? funcao.applyAsDouble(t * 2d) / 2d : 1 - funcao.applyAsDouble((1 - t) * 2d) / 2d;
    }

    private static double seno(double t) {
        return 1 - Math.cos(t * Math.PI / 2f);
    }

    private static double catmullRom(double n) {
        return 0.5d * (2d * (n + 1d) + 2d
                + (2d * n - 5d * (n + 1d) + 4d * (n + 2d) - (n + 3d))
                + (3d * (n + 1d) - n - 3d * (n + 2d) + (n + 3d)));
    }

    public static double pontoSpline(double t, double p0, double p1, double p2, double p3) {
        return 0.5d * (2d * p1 + (p2 - p0) * t
                + (2d * p0 - 5d * p1 + 4d * p2 - p3) * t * t
                + (3d * p1 - p0 - 3d * p2 + p3) * t * t * t);
    }

    private static DoubleUnaryOperator volta(Double n) {
        double fator = n == null ? 1.70158d : n * 1.70158d;
        return t -> t * t * ((fator + 1) * t - fator);
    }

    private static DoubleUnaryOperator elastico(Double n) {
        double fator = n == null ? 1 : n;
        return t -> 1 - Math.pow(Math.cos(t * Math.PI / 2f), 3) * Math.cos(t * fator * Math.PI);
    }

    private static DoubleUnaryOperator quique(Double n) {
        double f = n == null ? 0.5d : n;
        return t -> Math.min(
                Math.min(121f / 16f * t * t, 121f / 4f * f * Math.pow(t - 6f / 11f, 2) + 1 - f),
                Math.min(121 * f * f * Math.pow(t - 9f / 11f, 2) + 1 - f * f, 484 * f * f * f * Math.pow(t - 10.5f / 11f, 2) + 1 - f * f * f));
    }

    private static DoubleUnaryOperator degrau(Double n) {
        int passos = (int) (n == null ? 2 : Math.max(n, 2));
        double tamanho = 1 / (double) passos;
        return t -> {
            if (t < 0) return 0;
            double ultimo = (passos - 1) * tamanho;
            if (t > ultimo) return ultimo;
            return Math.floor(t / tamanho) * tamanho;
        };
    }
}
