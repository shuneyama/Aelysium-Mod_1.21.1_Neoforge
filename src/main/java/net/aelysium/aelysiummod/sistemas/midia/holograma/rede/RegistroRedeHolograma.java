package net.aelysium.aelysiummod.sistemas.midia.holograma.rede;

import net.aelysium.aelysiummod.sistemas.midia.holograma.rede.PacotesHolograma;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedeHolograma {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(
                PacotesHolograma.CriarHologramaS2C.TYPE,
                PacotesHolograma.CriarHologramaS2C.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.midia.holograma.cliente.ManipuladorHologramaCliente");
                        h.getMethod("handleSpawn",
                                PacotesHolograma.CriarHologramaS2C.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                PacotesHolograma.DestruirHologramaS2C.TYPE,
                PacotesHolograma.DestruirHologramaS2C.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.midia.holograma.cliente.ManipuladorHologramaCliente");
                        h.getMethod("handleDestroy",
                                PacotesHolograma.DestruirHologramaS2C.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                PacotesHolograma.AtualizarHologramaS2C.TYPE,
                PacotesHolograma.AtualizarHologramaS2C.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.midia.holograma.cliente.ManipuladorHologramaCliente");
                        h.getMethod("handleUpdate",
                                PacotesHolograma.AtualizarHologramaS2C.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
    }
}
