package net.aelysium.aelysiummod.sistemas.rp.raca.cliente;

import net.aelysium.aelysiummod.sistemas.rp.raca.rede.PacoteVooValkyria;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ManipuladoresRacaCliente {

    public static void handleValkyriaFlight(PacoteVooValkyria payload, IPayloadContext context) {
        context.enqueueWork(() ->
                RenderizadorHudValkyria.update(payload.visible(), payload.ticksFlown(), payload.cooldownTicks())
        );
    }
}
