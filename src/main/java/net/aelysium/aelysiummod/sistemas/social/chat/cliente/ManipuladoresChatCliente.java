package net.aelysium.aelysiummod.sistemas.social.chat.cliente;

import net.aelysium.aelysiummod.sistemas.social.chat.ConfiguracaoBalao;
import net.aelysium.aelysiummod.sistemas.social.chat.IJogadorAelysium;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.aelysium.aelysiummod.sistemas.social.chat.rede.RedeChat;

public class ManipuladoresChatCliente {

    public static void handleBalloonPacket(RedeChat.PacoteBalao payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Player player = mc.level.getPlayerByUUID(payload.playerUUID());
            if (player == null) return;
            ((IJogadorAelysium) player).aelysium$createBalloon(
                    payload.message(),
                    ConfiguracaoBalao.BALLOON_DURATION_TICKS,
                    payload.corTexto(),
                    payload.corFundo(),
                    payload.corBorda(),
                    payload.altura(),
                    payload.estilo()
            );
        });
    }

    public static void handleOpenBalloonGui(RedeChat.PacoteAbrirTelaBalao payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new TelaPersonalizacaoBalao(
                    payload.corTexto(),
                    payload.corFundo(),
                    payload.corBorda(),
                    payload.altura(),
                    payload.estilo()
            ));
        });
    }
}
