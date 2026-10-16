package net.aelysium.aelysiummod.conteudo.item.glock.rede;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.aelysium.aelysiummod.conteudo.item.glock.rede.PacoteRecarregarGlock;
import net.aelysium.aelysiummod.conteudo.item.glock.rede.PacoteRecuoGlock;
import net.aelysium.aelysiummod.conteudo.item.glock.rede.PacoteSomGlock;

public class RegistroRedeGlock {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(PacoteSomGlock.TYPE, PacoteSomGlock.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.conteudo.item.glock.cliente.ManipuladoresGlockCliente");
                        h.getMethod("handleGlockSom", PacoteSomGlock.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(PacoteRecuoGlock.TYPE, PacoteRecuoGlock.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.conteudo.item.glock.cliente.ManipuladoresGlockCliente");
                        h.getMethod("handleGlockRecuo", PacoteRecuoGlock.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToServer(PacoteRecarregarGlock.TYPE, PacoteRecarregarGlock.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        net.aelysium.aelysiummod.conteudo.item.glock.GlockItem.recarregar(player);
                    }
                }));
    }
}
