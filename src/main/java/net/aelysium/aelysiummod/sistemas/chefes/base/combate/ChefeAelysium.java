package net.aelysium.aelysiummod.sistemas.chefes.base.combate;

public interface ChefeAelysium {
    String chaveConfig();

    AlvosChefe getAlvos();

    default float multiplicadorDano(AtaqueChefe ataque) {
        return 1.0F;
    }
}
