package net.aelysium.aelysiummod.nucleo.jogador;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public interface ConsultaInsignia {

    String NENHUMA = "";

    String AUTOMATICA = "__auto__";

    MutableComponent componente(ServerPlayer jogador);

    boolean idPersonalizadoValido(String id);

    String substituicao(MinecraftServer servidor, UUID uuid);

    void definirSubstituicao(MinecraftServer servidor, UUID uuid, String valor);

    void limparSubstituicao(MinecraftServer servidor, UUID uuid);
}
