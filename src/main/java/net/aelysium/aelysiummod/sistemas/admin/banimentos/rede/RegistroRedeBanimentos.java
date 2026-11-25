package net.aelysium.aelysiummod.sistemas.admin.banimentos.rede;

import net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.RedeListaBanimentos;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.DadosBanimentoCliente;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedeBanimentos {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(RedeListaBanimentos.PacoteSincItensBanidos.TYPE, RedeListaBanimentos.PacoteSincItensBanidos.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> DadosBanimentoCliente.setBannedItems(payload.bannedItems())));

        registrar.playToClient(RedeListaBanimentos.PacoteSincItensPermitidos.TYPE, RedeListaBanimentos.PacoteSincItensPermitidos.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> DadosBanimentoCliente.setAllowedItems(payload.allowedItems())));

        registrar.playToServer(PacoteAcaoBanimento.TYPE, PacoteAcaoBanimento.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer j)
                        net.aelysium.aelysiummod.sistemas.admin.banimentos.GerenciadorBanimentosGui.tratar(j, pkt.acao(), pkt.alvo(), pkt.item());
                }));

        registrar.playToClient(PacoteEstadoAlvo.TYPE, PacoteEstadoAlvo.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.admin.banimentos.cliente.TelaItensBanidos");
                        h.getMethod("receber", PacoteEstadoAlvo.class).invoke(null, payload);
                    } catch (Exception e) { throw new RuntimeException(e); }
                }));

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.PacoteListaMods.TYPE,
                net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.PacoteListaMods.CODEC,
                net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.PacoteListaMods::handle);
    }
}
