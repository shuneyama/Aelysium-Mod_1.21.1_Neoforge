package net.aelysium.aelysiummod.network;

import net.aelysium.aelysiummod.chat.BalloonConfig;
import net.aelysium.aelysiummod.chat.IAelysiumPlayer;
import net.aelysium.aelysiummod.client.ClientNicknameHandler;
import net.aelysium.aelysiummod.client.ValkyriaHudRenderer;
import net.aelysium.aelysiummod.menu.gui.BalloonCustomizationScreen;
import net.aelysium.aelysiummod.menu.gui.NickEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientOnlyHandlers {

    public static void handleBalloonPacket(AelysiumNetwork.BalloonPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Player player = mc.level.getPlayerByUUID(payload.playerUUID());
            if (player == null) return;
            ((IAelysiumPlayer) player).aelysium$createBalloon(
                    payload.message(),
                    BalloonConfig.BALLOON_DURATION_TICKS,
                    payload.corTexto(),
                    payload.corFundo(),
                    payload.corBorda(),
                    payload.altura(),
                    payload.estilo()
            );
        });
    }

    public static void handleOpenBalloonGui(AelysiumNetwork.OpenBalloonGuiPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new BalloonCustomizationScreen(
                    payload.corTexto(),
                    payload.corFundo(),
                    payload.corBorda(),
                    payload.altura(),
                    payload.estilo()
            ));
        });
    }

    public static void handleNickSync(AelysiumNetwork.NickSyncPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.isRemoval()) {
                ClientNicknameHandler.remove(payload.targetUUID());
            } else {
                ClientNicknameHandler.set(
                        payload.targetUUID(),
                        payload.prefix(), payload.prefixCor1(), payload.prefixCor2(), payload.prefixFormat(),
                        payload.nick(), payload.nickCor(), payload.nickFormat(),
                        payload.suffix(), payload.suffixCor1(), payload.suffixCor2(), payload.suffixFormat()
                );
            }
        });
    }

    public static void handleNickEditorOpen(AelysiumNetwork.NickEditorOpenPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new NickEditorScreen(
                    payload.targetUUID(),
                    payload.realName(),
                    payload.prefix(), payload.prefixCor1(), payload.prefixCor2(), payload.prefixFormat(),
                    payload.nick(), payload.nickCor(), payload.nickFormat(),
                    payload.suffix(), payload.suffixCor1(), payload.suffixCor2(), payload.suffixFormat()
            ));
        });
    }

    public static void handleValkyriaFlight(ValkyriaFlightPacket payload, IPayloadContext context) {
        context.enqueueWork(() ->
                ValkyriaHudRenderer.update(payload.visible(), payload.ticksFlown(), payload.cooldownTicks())
        );
    }
}