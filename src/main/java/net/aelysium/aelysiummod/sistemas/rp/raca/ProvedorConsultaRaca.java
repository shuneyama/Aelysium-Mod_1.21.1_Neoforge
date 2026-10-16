package net.aelysium.aelysiummod.sistemas.rp.raca;

import net.aelysium.aelysiummod.nucleo.jogador.ConsultaRaca;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;

import java.util.UUID;

public class ProvedorConsultaRaca implements ConsultaRaca {

    @Override
    public TipoRaca raca(UUID uuid) {
        return TemporizadorRaca.getCachedRace(uuid);
    }
}
