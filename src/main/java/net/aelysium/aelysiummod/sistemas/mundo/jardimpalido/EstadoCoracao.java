package net.aelysium.aelysiummod.sistemas.mundo.jardimpalido;

import net.minecraft.util.StringRepresentable;

public enum EstadoCoracao implements StringRepresentable {
    UPROOTED("uprooted"),
    DORMANT("dormant"),
    AWAKE("awake");

    private final String nome;

    EstadoCoracao(String nome) {
        this.nome = nome;
    }

    @Override
    public String getSerializedName() {
        return nome;
    }
}
