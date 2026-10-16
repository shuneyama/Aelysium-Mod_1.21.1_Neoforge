package net.aelysium.aelysiummod.nucleo.jogador;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public interface ConsultaApelido {

    MutableComponent nomeCompletoEstilizado(MinecraftServer servidor, UUID uuid);

    MutableComponent nomeCompletoComPronomes(MinecraftServer servidor, UUID uuid);

    String apelido(MinecraftServer servidor, UUID uuid);

    String nomeExibicao(MinecraftServer servidor, UUID uuid);

    UUID uuidPorNomeOuApelido(MinecraftServer servidor, String nome);

    void atualizarListaJogadores(net.minecraft.server.level.ServerPlayer jogador);
}
