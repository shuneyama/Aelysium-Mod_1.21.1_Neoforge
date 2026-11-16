package net.aelysium.aelysiummod.compatibilidade.articulacoes.dobra;

import org.joml.Vector3f;

public final class PosicaoTemporaria implements PosicaoComOrigem {

    private final Vector3f original;
    private Vector3f resultado;

    public PosicaoTemporaria(Vector3f original) {
        this.original = original;
    }

    @Override
    public Vector3f posicaoOriginal() {
        return new Vector3f(original);
    }

    @Override
    public Vector3f posicao() {
        return resultado != null ? resultado : original;
    }

    @Override
    public void definirPosicao(Vector3f posicao) {
        this.resultado = posicao;
    }
}
