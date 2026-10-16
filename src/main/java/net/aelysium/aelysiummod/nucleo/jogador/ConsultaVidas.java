package net.aelysium.aelysiummod.nucleo.jogador;

import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public interface ConsultaVidas {

    boolean emLive(MinecraftServer servidor, UUID uuid);

    int corLive();
}
