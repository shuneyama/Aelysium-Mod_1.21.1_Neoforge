package net.aelysium.aelysiummod.sistemas.mundo.porta.rede;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedePorta {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(
                net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteAbrirPainelSenha.TYPE,
                net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteAbrirPainelSenha.CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.mundo.porta.cliente.ManipuladoresPortaSenhaCliente");
                        h.getMethod("handleAbrirPainel",
                                net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteAbrirPainelSenha.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteResultadoSenha.TYPE,
                net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteResultadoSenha.CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.mundo.porta.cliente.ManipuladoresPortaSenhaCliente");
                        h.getMethod("handleResultado",
                                net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteResultadoSenha.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteEnviarSenha.TYPE,
                net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteEnviarSenha.CODEC,
                net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteEnviarSenha::handle);

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteVincularCartao.TYPE,
                net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteVincularCartao.CODEC,
                net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteVincularCartao::handle);
    }
}
