package net.aelysium.aelysiummod.sistemas.social.apelido.rede;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.aelysium.aelysiummod.sistemas.social.apelido.rede.RedeApelido;
import net.aelysium.aelysiummod.sistemas.social.apelido.rede.RedePredefinicoesApelido;

public class RegistroRedeApelido {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(RedeApelido.PacoteSincApelido.TYPE, RedeApelido.PacoteSincApelido.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.social.apelido.cliente.ManipuladoresApelidoCliente");
                        h.getMethod("handleNickSync", RedeApelido.PacoteSincApelido.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(RedeApelido.PacoteAbrirEditorApelido.TYPE, RedeApelido.PacoteAbrirEditorApelido.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.social.apelido.cliente.ManipuladoresApelidoCliente");
                        h.getMethod("handleNickEditorOpen", RedeApelido.PacoteAbrirEditorApelido.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToServer(RedeApelido.PacoteSalvarEditorApelido.TYPE, RedeApelido.PacoteSalvarEditorApelido.STREAM_CODEC, RedeApelido::handleNickEditorSave);

        registrar.playToServer(RedePredefinicoesApelido.PacoteSalvarPredefinicao.TYPE, RedePredefinicoesApelido.PacoteSalvarPredefinicao.STREAM_CODEC, RedePredefinicoesApelido::handleSalvar);
        registrar.playToServer(RedePredefinicoesApelido.PacoteExcluirPredefinicao.TYPE, RedePredefinicoesApelido.PacoteExcluirPredefinicao.STREAM_CODEC, RedePredefinicoesApelido::handleExcluir);
        registrar.playToServer(RedePredefinicoesApelido.PacoteListarPredefinicoes.TYPE, RedePredefinicoesApelido.PacoteListarPredefinicoes.STREAM_CODEC, RedePredefinicoesApelido::handleListar);
        registrar.playToClient(RedePredefinicoesApelido.PacoteListaPredefinicoes.TYPE, RedePredefinicoesApelido.PacoteListaPredefinicoes.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.social.apelido.cliente.ManipuladoresApelidoCliente");
                        h.getMethod("handlePredefinicoes", RedePredefinicoesApelido.PacoteListaPredefinicoes.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
    }
}
