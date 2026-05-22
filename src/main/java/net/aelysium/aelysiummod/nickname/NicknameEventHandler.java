package net.aelysium.aelysiummod.nickname;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class NicknameEventHandler {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {
            player.getServer().execute(() -> {
                NicknameData data = NicknameData.get(player.getServer());

                data.syncToPlayer(player);

                for (ServerPlayer online : player.getServer().getPlayerList().getPlayers()) {
                    if (data.hasNickname(online.getUUID())) {
                        online.refreshTabListName();
                        var packet = new net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket(
                                net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                                online
                        );
                        player.connection.send(packet);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onTabListName(PlayerEvent.TabListNameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {
            NicknameData data = NicknameData.get(player.getServer());
            MutableComponent styled = data.getStyledFullName(player.getUUID());
            if (styled != null) {
                event.setDisplayName(styled);
            }
        }
    }

    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {
            NicknameData data = NicknameData.get(player.getServer());
            MutableComponent styled = data.getStyledFullName(player.getUUID());
            if (styled != null) {
                event.setDisplayname(styled);
            }
        }
    }
}