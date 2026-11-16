package net.aelysium.aelysiummod.compatibilidade.articulacoes.dobra;

import java.util.function.Consumer;

@FunctionalInterface
public interface IteravelPosicoes {

    void percorrerPosicoes(Consumer<PosicaoComOrigem> consumidor);
}
