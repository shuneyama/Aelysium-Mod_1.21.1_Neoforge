package net.aelysium.aelysiummod.compatibilidade.articulacoes.dobra;

import org.joml.Vector3f;

public final class Plano {

    private final Vector3f normal;
    private final float distanciaNormal;

    public Plano(Vector3f normal, Vector3f posicao) {
        this.normal = normal.normalize();
        this.distanciaNormal = -this.normal.dot(posicao);
    }

    public float distanciaAte(Vector3f posicao) {
        return normal.dot(posicao) + distanciaNormal;
    }
}
