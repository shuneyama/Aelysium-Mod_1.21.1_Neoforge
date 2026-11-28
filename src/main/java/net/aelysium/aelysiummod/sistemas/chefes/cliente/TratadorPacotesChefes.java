package net.aelysium.aelysiummod.sistemas.chefes.cliente;

import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.EfeitosBlocoFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.VoidBlossomEntity;
import net.aelysium.aelysiummod.sistemas.chefes.rede.PacoteColocarFlor;
import net.aelysium.aelysiummod.sistemas.chefes.rede.PacoteCuraFlor;
import net.aelysium.aelysiummod.sistemas.chefes.rede.PacoteEspinhosFlor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public final class TratadorPacotesChefes {

    private TratadorPacotesChefes() {
    }

    public static void curaFlor(PacoteCuraFlor pacote) {
        ClientLevel nivel = Minecraft.getInstance().level;
        if (nivel != null) EfeitosBlocoFlorVazio.handleVoidBlossomHeal(nivel, pacote.origem(), pacote.destino());
    }

    public static void colocarFlor(PacoteColocarFlor pacote) {
        EfeitosBlocoFlorVazio.handleVoidBlossomPlace(pacote.posicao());
    }

    public static void espinhosFlor(PacoteEspinhosFlor pacote) {
        ClientLevel nivel = Minecraft.getInstance().level;
        if (nivel != null && nivel.getEntity(pacote.entidade()) instanceof VoidBlossomEntity flor) {
            pacote.posicoes().forEach(flor.clientSpikeHandler::addSpike);
        }
    }
}
