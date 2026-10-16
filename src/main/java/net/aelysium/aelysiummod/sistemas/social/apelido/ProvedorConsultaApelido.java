package net.aelysium.aelysiummod.sistemas.social.apelido;

import net.aelysium.aelysiummod.nucleo.jogador.ConsultaApelido;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;
import net.aelysium.aelysiummod.sistemas.social.apelido.comando.ComandoApelido;

public class ProvedorConsultaApelido implements ConsultaApelido {

    @Override
    public MutableComponent nomeCompletoEstilizado(MinecraftServer servidor, UUID uuid) {
        return DadosApelido.get(servidor).getStyledFullName(uuid);
    }

    @Override
    public MutableComponent nomeCompletoComPronomes(MinecraftServer servidor, UUID uuid) {
        return DadosApelido.get(servidor).getStyledFullNameComPronomes(uuid);
    }

    @Override
    public String apelido(MinecraftServer servidor, UUID uuid) {
        DadosApelido.EntradaApelido entrada = DadosApelido.get(servidor).getNickname(uuid);
        return entrada == null ? null : entrada.nick();
    }

    @Override
    public String nomeExibicao(MinecraftServer servidor, UUID uuid) {
        return DadosApelido.resolverNomeExibicao(servidor, uuid);
    }

    @Override
    public UUID uuidPorNomeOuApelido(MinecraftServer servidor, String nome) {
        return DadosApelido.resolverUUIDPorNomeOuNick(servidor, nome);
    }

    @Override
    public void atualizarListaJogadores(net.minecraft.server.level.ServerPlayer jogador) {
        ComandoApelido.refreshTabList(jogador);
    }
}
