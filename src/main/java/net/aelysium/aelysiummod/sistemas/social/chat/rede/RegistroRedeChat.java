package net.aelysium.aelysiummod.sistemas.social.chat.rede;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.aelysium.aelysiummod.sistemas.social.chat.rede.RedeChat;

public class RegistroRedeChat {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(RedeChat.PacoteBalao.TYPE, RedeChat.PacoteBalao.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.social.chat.cliente.ManipuladoresChatCliente");
                        h.getMethod("handleBalloonPacket", RedeChat.PacoteBalao.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(RedeChat.PacoteAbrirTelaBalao.TYPE, RedeChat.PacoteAbrirTelaBalao.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.social.chat.cliente.ManipuladoresChatCliente");
                        h.getMethod("handleOpenBalloonGui", RedeChat.PacoteAbrirTelaBalao.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToServer(RedeChat.PacoteConfiguracaoBalao.TYPE, RedeChat.PacoteConfiguracaoBalao.STREAM_CODEC, RedeChat::handleBalloonConfig);
    }
}
