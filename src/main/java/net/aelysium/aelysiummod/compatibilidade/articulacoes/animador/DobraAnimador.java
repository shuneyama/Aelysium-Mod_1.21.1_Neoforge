package net.aelysium.aelysiummod.compatibilidade.articulacoes.animador;

import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.acesso.AcessoParteModelo;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.dobra.CuboideDobravel;

public final class DobraAnimador {

    private static final String MUTADOR_DOBRA = "dobra";
    private static final float DOBRA_MINIMA = 1.0E-4f;
    private static final int PIVO_CAPA = 6;

    public static void dobrar(ModelPart parte, float eixo, float valor) {
        if (!AelysiumGuarda.isAutorizadoAgora()) {
            return;
        }
        AcessoParteModelo.primeiroCuboide(parte).ifPresent(cuboide -> {
            if (Math.abs(valor) < DOBRA_MINIMA) {
                cuboide.cpmec$ativarMutador(null);
            } else if (cuboide.cpmec$ativarMutador(MUTADOR_DOBRA) instanceof CuboideDobravel dobravel) {
                dobravel.dobrar(eixo, valor);
            }
        });
    }

    public static void desativar(ModelPart parte) {
        AcessoParteModelo.primeiroCuboide(parte).ifPresent(cuboide -> cuboide.cpmec$ativarMutador(null));
    }

    public static void prepararDobra(ModelPart parte, Direction direcao) {
        AcessoParteModelo.primeiroCuboide(parte).ifPresent(cuboide ->
                cuboide.cpmec$registrarMutador(MUTADOR_DOBRA, dados -> CuboideDobravel.criar(dados, direcao)));
    }

    public static void prepararDobraCapa(ModelPart parte) {
        AcessoParteModelo.primeiroCuboide(parte).ifPresent(cuboide ->
                cuboide.cpmec$registrarMutador(MUTADOR_DOBRA, dados -> {
                    dados.pivo = PIVO_CAPA;
                    return CuboideDobravel.criar(dados, Direction.UP);
                }));
    }

    private DobraAnimador() {
    }
}
