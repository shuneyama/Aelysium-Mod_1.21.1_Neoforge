package net.aelysium.aelysiummod.compatibilidade.articulacoes.dobra;

import org.joml.Vector3f;

public interface PosicaoComOrigem {

    Vector3f posicaoOriginal();

    Vector3f posicao();

    void definirPosicao(Vector3f posicao);
}
