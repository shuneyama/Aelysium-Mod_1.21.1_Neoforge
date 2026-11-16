package net.aelysium.aelysiummod.compatibilidade.articulacoes.cuboide;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface CuboideMutavel {

    boolean cpmec$registrarMutador(String nome, Function<DadosCuboide, Cuboide> construtor);

    @Nullable
    Cuboide cpmec$ativarMutador(@Nullable String nome);

    @Nullable
    String cpmec$idMutadorAtivo();

    @Nullable
    Cuboide cpmec$mutadorAtivo();

    void cpmec$copiarEstadoDe(CuboideMutavel outro);
}
