package net.aelysium.aelysiummod.sistemas.social.maozinha.rede;

import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.sistemas.social.maozinha.GerenciadorMaozinha;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedeMaozinha {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToServer(PacoteAcionarMaozinha.TYPE, PacoteAcionarMaozinha.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (!AelysiumGuarda.isAutorizado()) return;
                    if (ctx.player() instanceof ServerPlayer jogador) {
                        GerenciadorMaozinha.acionar(jogador, pkt.alvoId());
                    }
                }));

        registrar.playToClient(PacoteEstadoMaozinha.TYPE, PacoteEstadoMaozinha.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.social.maozinha.cliente.ManipuladoresMaozinhaCliente");
                        h.getMethod("handleEstado", PacoteEstadoMaozinha.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
    }
}
