package net.aelysium.aelysiummod.sistemas.mundo.minifarol.cliente;

import net.aelysium.aelysiummod.sistemas.mundo.minifarol.rede.PacoteAbrirMiniFarol;
import net.minecraft.client.Minecraft;

public class ManipuladorMiniFarolCliente {

    public static void abrir(PacoteAbrirMiniFarol pacote) {
        Minecraft.getInstance().setScreen(new TelaMiniFarol(pacote.pos(), pacote.ativo(), pacote.cor()));
    }
}
