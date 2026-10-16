package net.aelysium.aelysiummod.sistemas.rp.vidas;

import net.aelysium.aelysiummod.nucleo.jogador.ConsultaVidas;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public class ProvedorConsultaVidas implements ConsultaVidas {

    @Override
    public boolean emLive(MinecraftServer servidor, UUID uuid) {
        return DadosVidas.get(servidor).isLive(uuid);
    }

    @Override
    public int corLive() {
        return DadosVidas.COR_LIVE;
    }
}
