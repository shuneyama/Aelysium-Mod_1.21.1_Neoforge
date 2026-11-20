package net.aelysium.aelysiummod.sistemas.rp.raca.cliente;

import net.aelysium.aelysiummod.sistemas.rp.raca.rede.PacoteAbrirSelecaoRaca;
import net.minecraft.client.Minecraft;

public class ManipuladorSelecaoRacaCliente {

    public static void abrir(PacoteAbrirSelecaoRaca pacote) {
        Minecraft.getInstance().setScreen(new TelaSelecaoRaca(pacote.atual()));
    }
}
