package net.aelysium.aelysiummod.sistemas.rp.raca.rede;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.aelysium.aelysiummod.sistemas.rp.raca.rede.PacoteAtivarVisaoNoturna;
import net.aelysium.aelysiummod.sistemas.rp.raca.rede.PacoteVooValkyria;

public class RegistroRedeRaca {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(PacoteVooValkyria.TYPE, PacoteVooValkyria.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.rp.raca.cliente.ManipuladoresRacaCliente");
                        h.getMethod("handleValkyriaFlight", PacoteVooValkyria.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(PacoteImunidadeFogo.TYPE, PacoteImunidadeFogo.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.rp.raca.cliente.RenderizadorFogoRaca");
                        h.getMethod("definir", boolean.class).invoke(null, payload.ativa());
                    } catch (Exception e) { throw new RuntimeException(e); }
                }));

        registrar.playToServer(PacoteAtivarVisaoNoturna.TYPE, PacoteAtivarVisaoNoturna.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        net.aelysium.aelysiummod.sistemas.rp.raca.AlternadorVisaoNoturna.alternar(player);
                    }
                }));
    }
}
