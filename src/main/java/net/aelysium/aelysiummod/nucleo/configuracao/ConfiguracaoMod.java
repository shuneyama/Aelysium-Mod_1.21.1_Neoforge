package net.aelysium.aelysiummod.nucleo.configuracao;

import net.aelysium.aelysiummod.sistemas.admin.historico.ConfiguracaoRegrasMods;

public class ConfiguracaoMod {

    public static void setJoinLeaveMessagesEnabled(boolean enabled) {
        ConfiguracaoRegrasMods.definirMensagensEntradaSaida(enabled);
    }

    public static boolean areJoinLeaveMessagesEnabled() {
        return ConfiguracaoRegrasMods.mensagensEntradaSaida();
    }

    public static boolean toggleJoinLeaveMessages() {
        setJoinLeaveMessagesEnabled(!areJoinLeaveMessagesEnabled());
        return areJoinLeaveMessagesEnabled();
    }
}
