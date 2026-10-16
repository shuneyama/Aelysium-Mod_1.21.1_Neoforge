package net.aelysium.aelysiummod.sistemas.rp.habilidade;

import net.aelysium.aelysiummod.nucleo.jogador.ConsultaHabilidades;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class ProvedorConsultaHabilidades implements ConsultaHabilidades {

    @Override
    public boolean estaMudo(UUID uuid) {
        return GerenciadorHabilidade.estaMudo(uuid);
    }

    @Override
    public boolean manutencaoAtiva() {
        return GerenciadorHabilidade.isManutencaoAtiva();
    }

    @Override
    public boolean temVoo(ServerPlayer jogador) {
        return GerenciadorHabilidade.temVoo(jogador);
    }
}
