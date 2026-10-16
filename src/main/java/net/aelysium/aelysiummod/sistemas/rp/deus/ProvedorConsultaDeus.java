package net.aelysium.aelysiummod.sistemas.rp.deus;

import net.aelysium.aelysiummod.nucleo.jogador.ConsultaDeus;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.minecraft.server.level.ServerPlayer;

public class ProvedorConsultaDeus implements ConsultaDeus {

    @Override
    public TipoDeus deus(ServerPlayer jogador) {
        return DadosDeus.get(jogador.serverLevel()).getDeus(jogador.getUUID());
    }
}
