package net.aelysium.aelysiummod.compatibilidade.articulacoes.cpm;

import net.aelysium.aelysiummod.compatibilidade.articulacoes.dobra.CuboideDobravel;
import org.joml.Matrix4f;

public final class ContextoDobra {

    private static CuboideDobravel atual;
    private static Matrix4f referencia;

    public static void definir(CuboideDobravel cuboide) {
        atual = cuboide;
        referencia = null;
    }

    public static void definirReferencia(Matrix4f matriz) {
        referencia = matriz;
    }

    public static void limpar() {
        atual = null;
        referencia = null;
    }

    public static CuboideDobravel obter() {
        return atual;
    }

    public static Matrix4f obterReferencia() {
        return referencia;
    }

    private ContextoDobra() {
    }
}
