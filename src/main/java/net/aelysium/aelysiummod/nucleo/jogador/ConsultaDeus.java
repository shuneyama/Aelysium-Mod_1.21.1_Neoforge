package net.aelysium.aelysiummod.nucleo.jogador;

import net.minecraft.server.level.ServerPlayer;

public interface ConsultaDeus {

    TipoDeus deus(ServerPlayer jogador);
}
