package net.aelysium.aelysiummod.sistemas.rp.morte.cliente;

import net.aelysium.aelysiummod.sistemas.rp.morte.rede.PacoteImpactoMorte;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ManipuladoresMorteCliente {

    public static void handleMorteImpacto(PacoteImpactoMorte payload, IPayloadContext context) {
        context.enqueueWork(() -> RenderizadorSobreposicaoMorte.triggerImpacto(payload.habilitando()));
    }
}
