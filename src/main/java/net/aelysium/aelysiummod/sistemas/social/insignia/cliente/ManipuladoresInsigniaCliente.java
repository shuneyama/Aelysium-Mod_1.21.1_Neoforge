package net.aelysium.aelysiummod.sistemas.social.insignia.cliente;

import net.aelysium.aelysiummod.nucleo.cliente.InsigniasPersonalizadasCliente;
import net.aelysium.aelysiummod.sistemas.social.insignia.rede.RedeInsignia;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ManipuladoresInsigniaCliente {

    public static void handleSync(RedeInsignia.PacoteSincInsignias payload, IPayloadContext context) {
        context.enqueueWork(() -> InsigniasPersonalizadasCliente.substituir(payload.insignias()));
    }
}
