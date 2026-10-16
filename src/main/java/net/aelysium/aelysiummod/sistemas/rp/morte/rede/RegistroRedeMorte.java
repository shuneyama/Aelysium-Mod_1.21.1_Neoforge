package net.aelysium.aelysiummod.sistemas.rp.morte.rede;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.aelysium.aelysiummod.sistemas.rp.morte.rede.PacoteImpactoMorte;

public class RegistroRedeMorte {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(PacoteImpactoMorte.TYPE, PacoteImpactoMorte.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.rp.morte.cliente.ManipuladoresMorteCliente");
                        h.getMethod("handleMorteImpacto", PacoteImpactoMorte.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
    }
}
