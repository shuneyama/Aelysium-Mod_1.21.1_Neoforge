package net.aelysium.aelysiummod.nucleo.jogador;

public final class PerfilJogador {

    private static ConsultaRaca raca;
    private static ConsultaDeus deus;
    private static ConsultaHabilidades habilidades;
    private static ConsultaApelido apelido;
    private static ConsultaInsignia insignia;
    private static ConsultaVidas vidas;
    private static ConsultaVoo voo;

    private PerfilJogador() {
    }

    public static void registrarRaca(ConsultaRaca consulta) { raca = consulta; }

    public static void registrarDeus(ConsultaDeus consulta) { deus = consulta; }

    public static void registrarHabilidades(ConsultaHabilidades consulta) { habilidades = consulta; }

    public static void registrarApelido(ConsultaApelido consulta) { apelido = consulta; }

    public static void registrarInsignia(ConsultaInsignia consulta) { insignia = consulta; }

    public static void registrarVidas(ConsultaVidas consulta) { vidas = consulta; }

    public static void registrarVoo(ConsultaVoo consulta) { voo = consulta; }

    public static ConsultaVoo voo() { return exigir(voo, "voo"); }

    public static ConsultaRaca raca() { return exigir(raca, "raca"); }

    public static ConsultaDeus deus() { return exigir(deus, "deus"); }

    public static ConsultaHabilidades habilidades() { return exigir(habilidades, "habilidades"); }

    public static ConsultaApelido apelido() { return exigir(apelido, "apelido"); }

    public static ConsultaInsignia insignia() { return exigir(insignia, "insignia"); }

    public static ConsultaVidas vidas() { return exigir(vidas, "vidas"); }

    private static <T> T exigir(T consulta, String nome) {
        if (consulta == null) throw new IllegalStateException("PerfilJogador." + nome + " nao registrado");
        return consulta;
    }
}
