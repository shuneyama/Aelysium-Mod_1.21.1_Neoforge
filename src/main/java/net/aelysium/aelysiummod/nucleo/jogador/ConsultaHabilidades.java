package net.aelysium.aelysiummod.nucleo.jogador;

import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public interface ConsultaHabilidades {

    boolean estaMudo(UUID uuid);

    boolean manutencaoAtiva();

    boolean temVoo(ServerPlayer jogador);
}
