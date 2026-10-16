package net.aelysium.aelysiummod.sistemas.social.insignia;

import net.aelysium.aelysiummod.nucleo.jogador.ConsultaInsignia;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class ProvedorConsultaInsignia implements ConsultaInsignia {

    @Override
    public MutableComponent componente(ServerPlayer jogador) {
        return GerenciadorInsignias.getBadgeComponent(jogador);
    }

    @Override
    public boolean idPersonalizadoValido(String id) {
        return GerenciadorInsigniasPersonalizadas.porId(id) != null;
    }

    @Override
    public String substituicao(MinecraftServer servidor, UUID uuid) {
        return DadosInsignia.get(servidor).getOverride(uuid);
    }

    @Override
    public void definirSubstituicao(MinecraftServer servidor, UUID uuid, String valor) {
        DadosInsignia.get(servidor).setOverride(uuid, valor);
    }

    @Override
    public void limparSubstituicao(MinecraftServer servidor, UUID uuid) {
        DadosInsignia.get(servidor).clearOverride(uuid);
    }
}
