package net.aelysium.aelysiummod.jade;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record OpPlayersSyncPacket(Set<UUID> opPlayers) implements CustomPacketPayload {

    public static final Type<OpPlayersSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aelysiummod", "op_players_sync"));

    public static final StreamCodec<ByteBuf, OpPlayersSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.fromCodec(UUIDUtil.CODEC)),
            OpPlayersSyncPacket::opPlayers,
            OpPlayersSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpPlayersSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            OpPlayersManager.setOpPlayers(packet.opPlayers());
        });
    }
}