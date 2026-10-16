package net.aelysium.aelysiummod.sistemas.midia.tocar.rede;

import net.aelysium.aelysiummod.sistemas.midia.tocar.rede.PacotesSom;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedeTocar {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(PacotesSom.PacoteIniciarSom.TYPE, PacotesSom.PacoteIniciarSom.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.midia.tocar.cliente.ManipuladorTocarCliente");
                        h.getMethod("handleIniciar", PacotesSom.PacoteIniciarSom.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(PacotesSom.PacoteChunkSom.TYPE, PacotesSom.PacoteChunkSom.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.midia.tocar.cliente.ManipuladorTocarCliente");
                        h.getMethod("handleChunk", PacotesSom.PacoteChunkSom.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(PacotesSom.PacoteTocarSom.TYPE, PacotesSom.PacoteTocarSom.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.midia.tocar.cliente.ManipuladorTocarCliente");
                        h.getMethod("handleTocar", PacotesSom.PacoteTocarSom.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(PacotesSom.PacoteParar.TYPE, PacotesSom.PacoteParar.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.midia.tocar.cliente.ManipuladorTocarCliente");
                        h.getMethod("handleParar", PacotesSom.PacoteParar.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
    }
}
