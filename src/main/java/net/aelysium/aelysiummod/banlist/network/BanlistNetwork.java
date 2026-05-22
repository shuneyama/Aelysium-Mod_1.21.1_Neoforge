package net.aelysium.aelysiummod.banlist.network;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.aelysium.aelysiummod.banlist.config.BanlistConfig;
import net.aelysium.aelysiummod.banlist.data.PlayerBanData;

import java.util.HashSet;
import java.util.Set;

public class BanlistNetwork {

    public static final ResourceLocation SYNC_BANNED_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "banlist_sync_banned");
    public static final ResourceLocation SYNC_ALLOWED_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "banlist_sync_allowed");

    public record SyncBannedItemsPacket(Set<ResourceLocation> bannedItems) implements CustomPacketPayload {
        public static final Type<SyncBannedItemsPacket> TYPE = new Type<>(SYNC_BANNED_ID);
        public static final StreamCodec<FriendlyByteBuf, SyncBannedItemsPacket> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public SyncBannedItemsPacket decode(FriendlyByteBuf buf) {
                int size = buf.readInt();
                Set<ResourceLocation> items = new HashSet<>();
                for (int i = 0; i < size; i++) {
                    items.add(buf.readResourceLocation());
                }
                return new SyncBannedItemsPacket(items);
            }
            @Override
            public void encode(FriendlyByteBuf buf, SyncBannedItemsPacket p) {
                buf.writeInt(p.bannedItems.size());
                for (ResourceLocation rl : p.bannedItems) {
                    buf.writeResourceLocation(rl);
                }
            }
        };
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record SyncAllowedItemsPacket(Set<ResourceLocation> allowedItems) implements CustomPacketPayload {
        public static final Type<SyncAllowedItemsPacket> TYPE = new Type<>(SYNC_ALLOWED_ID);
        public static final StreamCodec<FriendlyByteBuf, SyncAllowedItemsPacket> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public SyncAllowedItemsPacket decode(FriendlyByteBuf buf) {
                int size = buf.readInt();
                Set<ResourceLocation> items = new HashSet<>();
                for (int i = 0; i < size; i++) {
                    items.add(buf.readResourceLocation());
                }
                return new SyncAllowedItemsPacket(items);
            }
            @Override
            public void encode(FriendlyByteBuf buf, SyncAllowedItemsPacket p) {
                buf.writeInt(p.allowedItems.size());
                for (ResourceLocation rl : p.allowedItems) {
                    buf.writeResourceLocation(rl);
                }
            }
        };
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void syncToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncBannedItemsPacket(BanlistConfig.getBannedItems()));

        PlayerBanData data = PlayerBanData.get(player.getServer());
        Set<ResourceLocation> allowed = data.getAllowedItems(player.getUUID());
        PacketDistributor.sendToPlayer(player, new SyncAllowedItemsPacket(allowed));
    }

    public static void syncAllPlayers(net.minecraft.server.MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncToPlayer(player);
        }
    }
}
