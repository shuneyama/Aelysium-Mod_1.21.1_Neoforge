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

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.PacoteListaMods.TYPE,
                net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.PacoteListaMods.CODEC,
                net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.PacoteListaMods::handle);
    }
}
