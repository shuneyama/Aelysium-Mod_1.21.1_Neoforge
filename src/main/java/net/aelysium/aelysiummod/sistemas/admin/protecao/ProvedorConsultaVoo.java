package net.aelysium.aelysiummod.sistemas.admin.protecao;

import net.aelysium.aelysiummod.nucleo.jogador.ConsultaVoo;
import net.minecraft.server.level.ServerPlayer;
import net.aelysium.aelysiummod.sistemas.admin.protecao.evento.ControleRegiaoJogador;

public class ProvedorConsultaVoo implements ConsultaVoo {

    @Override
    public boolean podeVoarAqui(ServerPlayer jogador) {
        return ControleRegiaoJogador.podeVoarAqui(jogador);
    }
}
