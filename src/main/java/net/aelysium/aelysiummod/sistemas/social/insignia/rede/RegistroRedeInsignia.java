package net.aelysium.aelysiummod.sistemas.social.insignia.rede;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedeInsignia {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToServer(RedeInsignia.PacoteCriarInsignia.TYPE, RedeInsignia.PacoteCriarInsignia.STREAM_CODEC, RedeInsignia::handleCriar);
        registrar.playToServer(RedeInsignia.PacoteExcluirInsignia.TYPE, RedeInsignia.PacoteExcluirInsignia.STREAM_CODEC, RedeInsignia::handleExcluir);
        registrar.playToClient(RedeInsignia.PacoteSincInsignias.TYPE, RedeInsignia.PacoteSincInsignias.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.social.insignia.cliente.ManipuladoresInsigniaCliente");
                        h.getMethod("handleSync", RedeInsignia.PacoteSincInsignias.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
    }
}
