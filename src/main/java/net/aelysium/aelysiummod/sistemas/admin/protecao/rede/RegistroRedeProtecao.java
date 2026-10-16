package net.aelysium.aelysiummod.sistemas.admin.protecao.rede;

import net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteSyncRegioesCliente;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedeProtecao {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(
                PacoteSyncRegioesCliente.TYPE,
                PacoteSyncRegioesCliente.CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.ManipuladoresProtecaoCliente");
                        h.getMethod("handleSyncRegioes",
                                PacoteSyncRegioesCliente.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteCriarRegiao.TYPE,
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteCriarRegiao.CODEC,
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteCriarRegiao::handle);

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteEditarRegiao.TYPE,
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteEditarRegiao.CODEC,
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteEditarRegiao::handle);

        registrar.playToClient(
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteAbrirTela.TYPE,
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteAbrirTela.CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.ManipuladoresProtecaoCliente");
                        h.getMethod("handleAbrirTela",
                                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteAbrirTela.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteAbrirTelaEdicao.TYPE,
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteAbrirTelaEdicao.CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.ManipuladoresProtecaoCliente");
                        h.getMethod("handleAbrirTelaEdicao",
                                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteAbrirTelaEdicao.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteListaRegioes.TYPE,
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteListaRegioes.CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.ManipuladoresProtecaoCliente");
                        h.getMethod("handleListaRegioes",
                                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteListaRegioes.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteDeletarRegiao.TYPE,
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteDeletarRegiao.CODEC,
                net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteDeletarRegiao::handle);
    }
}
