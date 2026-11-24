package net.aelysium.aelysiummod.sistemas.rp.skin.rede;

import net.aelysium.aelysiummod.sistemas.rp.skin.GerenciadorSkins;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class RegistroRedeSkin {

    private RegistroRedeSkin() {
    }

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToServer(PacoteEnviarSkin.TYPE, PacoteEnviarSkin.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer j)
                        GerenciadorSkins.receberParte(j, pkt.tipo(), pkt.id(), pkt.indice(), pkt.total(), pkt.modelo(), pkt.parte());
                }));

        registrar.playToServer(PacoteRemoverSkin.TYPE, PacoteRemoverSkin.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer j) GerenciadorSkins.remover(j, pkt.tipo(), pkt.id());
                }));

        registrar.playToServer(PacoteSolicitarInfoSkin.TYPE, PacoteSolicitarInfoSkin.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer j) GerenciadorSkins.solicitarInfo(j, pkt.tipo(), pkt.id());
                }));

        registrar.playToServer(PacoteEstiloSkin.TYPE, PacoteEstiloSkin.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer j) GerenciadorSkins.definirEstilo(j, pkt.tipo(), pkt.id(), pkt.modelo());
                }));

        registrar.playToClient(PacoteInfoSkin.TYPE, PacoteInfoSkin.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.rp.skin.cliente.TelaSkin");
                        h.getMethod("receber", String.class, String.class, boolean.class, String.class, boolean.class)
                                .invoke(null, payload.tipo(), payload.id(), payload.definida(), payload.modelo(), payload.cpm());
                    } catch (Exception e) { throw new RuntimeException(e); }
                }));

        registrar.playToClient(PacoteSkinJogador.TYPE, PacoteSkinJogador.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.rp.skin.cliente.SkinsJogadoresCliente");
                        h.getMethod("receber", java.util.UUID.class, String.class, byte[].class)
                                .invoke(null, payload.jogador(), payload.modelo(), payload.dados());
                    } catch (Exception e) { throw new RuntimeException(e); }
                }));
    }
}
