package net.aelysium.aelysiummod.sistemas.social.npc.rede;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedeNpc {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.AbrirEditorNpcS2C.TYPE,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.AbrirEditorNpcS2C.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.social.npc.cliente.ManipuladoresNpcCliente");
                        h.getMethod("handleOpenEditor",
                                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.AbrirEditorNpcS2C.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.SincSkinNpcS2C.TYPE,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.SincSkinNpcS2C.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.social.npc.cliente.ManipuladoresNpcCliente");
                        h.getMethod("handleSyncSkin",
                                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.SincSkinNpcS2C.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.SincTrocasNpcS2C.TYPE,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.SincTrocasNpcS2C.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.social.npc.cliente.ManipuladoresNpcCliente");
                        h.getMethod("handleSyncTrades",
                                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.SincTrocasNpcS2C.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.AtualizarTrocasNpcC2S.TYPE,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.AtualizarTrocasNpcC2S.STREAM_CODEC,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc::handleUpdateTrades);

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.AtualizarConfiguracoesNpcC2S.TYPE,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.AtualizarConfiguracoesNpcC2S.STREAM_CODEC,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc::handleUpdateSettings);

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.EnviarSkinNpcC2S.TYPE,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.EnviarSkinNpcC2S.STREAM_CODEC,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc::handleUploadSkin);

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.EnviarModeloCpmNpcC2S.TYPE,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.EnviarModeloCpmNpcC2S.STREAM_CODEC,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc::handleUploadCpm);

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.PedirArquivoNpcC2S.TYPE,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.PedirArquivoNpcC2S.STREAM_CODEC,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc::handlePedirArquivo);

        registrar.playToClient(
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.ArquivoNpcS2C.TYPE,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.ArquivoNpcS2C.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.social.npc.cliente.ManipuladoresNpcCliente");
                        h.getMethod("handleArquivoNpc",
                                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.ArquivoNpcS2C.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.RemoverNpcC2S.TYPE,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.RemoverNpcC2S.STREAM_CODEC,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc::handleRemoveNpc);

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.VincularLivroRegistroC2S.TYPE,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.VincularLivroRegistroC2S.STREAM_CODEC,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc::handleLinkLogBook);

        registrar.playToServer(
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.AtualizarEquipamentoNpcC2S.TYPE,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.AtualizarEquipamentoNpcC2S.STREAM_CODEC,
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc::handleUpdateEquipment);
    }
}
