package net.aelysium.aelysiummod.compatibilidade.articulacoes.dobra;

import org.joml.Vector3f;

public final class PosicaoMemorizada implements PosicaoComOrigem {

    private final Vector3f origem;
    private Vector3f atual;

    public PosicaoMemorizada(Vector3f origem) {
        this.origem = origem;
    }

    @Override
    public Vector3f posicaoOriginal() {
        return new Vector3f(origem);
    }

    @Override
    public Vector3f posicao() {
        return atual;
    }

    @Override
    public void definirPosicao(Vector3f posicao) {
        this.atual = posicao;
    }
}
