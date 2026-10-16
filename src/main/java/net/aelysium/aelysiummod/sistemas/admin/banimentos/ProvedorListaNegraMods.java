package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import net.aelysium.aelysiummod.nucleo.servico.ListaNegraMods;
import net.aelysium.aelysiummod.sistemas.admin.historico.ConfiguracaoRegrasMods;

public class ProvedorListaNegraMods implements ListaNegraMods {

    @Override
    public boolean alternar(String modId) {
        return ConfiguracaoRegrasMods.alternarProibido(modId);
    }
}
