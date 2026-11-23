package net.aelysium.aelysiummod.sistemas.rp.deus.rede;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.aelysium.aelysiummod.sistemas.rp.deus.rede.PacoteAtivarFormaDivina;
import net.aelysium.aelysiummod.sistemas.rp.deus.rede.PacoteAtivarVanish;
import net.aelysium.aelysiummod.sistemas.rp.deus.rede.PacoteFormaDivina;

public class RegistroRedeDeus {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(PacoteFormaDivina.TYPE, PacoteFormaDivina.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.rp.deus.cliente.CacheFormaDivina");
                        h.getMethod("set", java.util.UUID.class, boolean.class, String.class, int.class)
                                .invoke(null, payload.uuid(), payload.ativa(), payload.deusId(), payload.cor());
                    } catch (Exception e) { throw new RuntimeException(e); }
                }));

        registrar.playToServer(PacoteSolicitarDeuses.TYPE, PacoteSolicitarDeuses.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer j)
                        net.aelysium.aelysiummod.sistemas.rp.deus.GerenciadorDeuses.enviarLista(j);
                }));

        registrar.playToServer(PacoteEscolherDeus.TYPE, PacoteEscolherDeus.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer j)
                        net.aelysium.aelysiummod.sistemas.rp.deus.GerenciadorDeuses.escolher(j, pkt.id());
                }));

        registrar.playToServer(PacoteCriarDeus.TYPE, PacoteCriarDeus.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer j)
                        net.aelysium.aelysiummod.sistemas.rp.deus.GerenciadorDeuses.criar(j, pkt.nome(), pkt.cor());
                }));

        registrar.playToClient(PacoteListaDeuses.TYPE, PacoteListaDeuses.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.rp.deus.cliente.TelaDeuses");
                        h.getMethod("receber", java.util.List.class, String.class).invoke(null, payload.deuses(), payload.atual());
                    } catch (Exception e) { throw new RuntimeException(e); }
                }));

        registrar.playToServer(PacoteAtivarFormaDivina.TYPE, PacoteAtivarFormaDivina.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer j)
                        net.aelysium.aelysiummod.sistemas.rp.deus.FormaDivina.tentar(j);
                }));

        registrar.playToServer(PacoteAtivarVanish.TYPE, PacoteAtivarVanish.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        net.aelysium.aelysiummod.sistemas.rp.deus.DadosDeus data =
                                net.aelysium.aelysiummod.sistemas.rp.deus.DadosDeus.get(player.serverLevel());
                        net.aelysium.aelysiummod.nucleo.jogador.TipoDeus deus = data.getDeus(player.getUUID());
                        if (deus != net.aelysium.aelysiummod.nucleo.jogador.TipoDeus.NONE) {
                            net.aelysium.aelysiummod.sistemas.rp.deus.ManipuladorTransicaoVanish.startTransition(player, deus);
                        }
                    }
                }));
    }
}
