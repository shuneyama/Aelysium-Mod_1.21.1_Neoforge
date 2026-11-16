package net.aelysium.aelysiummod.sistemas.admin.cabeca.cliente;

import net.aelysium.aelysiummod.sistemas.admin.cabeca.rede.PacoteAbrirCabeca;
import net.minecraft.client.Minecraft;

public class ManipuladorCabecaCliente {

    public static void abrir(PacoteAbrirCabeca pacote) {
        Minecraft.getInstance().setScreen(new TelaCabecaJogador(pacote.pos(), pacote.atual()));
    }
}
