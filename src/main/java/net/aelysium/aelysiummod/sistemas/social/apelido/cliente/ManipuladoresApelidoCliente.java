package net.aelysium.aelysiummod.sistemas.social.apelido.cliente;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.aelysium.aelysiummod.sistemas.social.apelido.rede.RedeApelido;

public class ManipuladoresApelidoCliente {

    public static void handleNickSync(RedeApelido.PacoteSincApelido payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.isRemoval()) {
                ManipuladorApelidoCliente.remove(payload.targetUUID());
            } else {
                ManipuladorApelidoCliente.set(
                        payload.targetUUID(),
                        payload.prefix(), payload.prefixCor1(), payload.prefixCor2(), payload.prefixFormat(),
                        payload.nick(), payload.nickCor(), payload.nickFormat(),
                        payload.suffix(), payload.suffixCor1(), payload.suffixCor2(), payload.suffixFormat()
                );
            }
        });
    }

    public static void handlePredefinicoes(net.aelysium.aelysiummod.sistemas.social.apelido.rede.RedePredefinicoesApelido.PacoteListaPredefinicoes payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof TelaPredefinicoesApelido lista) {
                lista.atualizar(payload.predefinicoes());
            } else if (mc.screen instanceof TelaEditorApelido editor) {
                mc.setScreen(new TelaPredefinicoesApelido(editor, payload.predefinicoes(), editor::aplicarPredefinicao));
            }
        });
    }

    public static void handleNickEditorOpen(RedeApelido.PacoteAbrirEditorApelido payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new TelaEditorApelido(
                    payload.targetUUID(),
                    payload.realName(),
                    payload.prefix(), payload.prefixCor1(), payload.prefixCor2(), payload.prefixFormat(),
                    payload.nick(), payload.nickCor(), payload.nickFormat(),
                    payload.suffix(), payload.suffixCor1(), payload.suffixCor2(), payload.suffixFormat(),
                    payload.badgeOverride(), payload.pronomes()
            ));
        });
    }
}
