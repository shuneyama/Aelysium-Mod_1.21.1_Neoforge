package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;

public final class SonsAtualizados {

    public static final SoundEvent SPAWNER_QUEBRAR = evento("spawner.quebrar");
    public static final SoundEvent SPAWNER_PASSO = evento("spawner.passo");
    public static final SoundEvent BOLHA_ESTOURAR = evento("bolha.estourar");
    public static final SoundEvent MOCHILA_FALHA = evento("mochila.insert_fail");

    public static final SoundType SPAWNER = new SoundType(1.0F, 1.0F, SPAWNER_QUEBRAR, SPAWNER_PASSO, SPAWNER_QUEBRAR, SPAWNER_PASSO, SPAWNER_PASSO);

    private SonsAtualizados() {
    }

    private static SoundEvent evento(String nome) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, nome));
    }
}
