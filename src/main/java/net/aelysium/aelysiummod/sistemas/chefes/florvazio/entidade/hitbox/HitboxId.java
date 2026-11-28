package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.hitbox;

public enum HitboxId {
    Idle((byte) 0),
    Spike((byte) 1),
    Petal((byte) 2),
    SpikeWave1((byte) 3),
    SpikeWave2((byte) 4),
    SpikeWave3((byte) 5),
    Spore((byte) 6);

    private final byte id;

    HitboxId(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }
}
