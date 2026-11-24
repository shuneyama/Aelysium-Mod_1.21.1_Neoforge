package net.aelysium.aelysiummod.sistemas.rp.personagem.rede;

import net.aelysium.aelysiummod.sistemas.rp.personagem.GerenciadorPersonagens;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class RegistroRedePersonagens {

    private RegistroRedePersonagens() {
    }

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToServer(PacoteSolicitarPersonagens.TYPE, PacoteSolicitarPersonagens.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer j) GerenciadorPersonagens.enviarLista(j);
                }));

        registrar.playToServer(PacoteEscolherPersonagem.TYPE, PacoteEscolherPersonagem.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer j) GerenciadorPersonagens.escolher(j, pkt.id());
                }));

        registrar.playToServer(PacoteSalvarPersonagem.TYPE, PacoteSalvarPersonagem.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer j) GerenciadorPersonagens.salvar(j, pkt.personagem());
                }));

        registrar.playToServer(PacoteExcluirPersonagem.TYPE, PacoteExcluirPersonagem.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer j) GerenciadorPersonagens.excluir(j, pkt.id());
                }));

        registrar.playToClient(PacoteListaPersonagens.TYPE, PacoteListaPersonagens.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.rp.personagem.cliente.TelaPersonagens");
                        h.getMethod("receber", java.util.List.class, String.class).invoke(null, payload.personagens(), payload.atual());
                    } catch (Exception e) { throw new RuntimeException(e); }
                }));
    }
}
