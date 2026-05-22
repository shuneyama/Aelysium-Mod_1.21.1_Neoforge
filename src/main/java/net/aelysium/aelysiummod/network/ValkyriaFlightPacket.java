package net.aelysium.aelysiummod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ValkyriaFlightPacket(boolean visible, int ticksFlown, int cooldownTicks) implements CustomPacketPayload {

    public static final Type<ValkyriaFlightPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aelysiummod", "valkyria_flight"));

    public static final StreamCodec<FriendlyByteBuf, ValkyriaFlightPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ValkyriaFlightPacket decode(FriendlyByteBuf buf) {
            return new ValkyriaFlightPacket(buf.readBoolean(), buf.readInt(), buf.readInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, ValkyriaFlightPacket p) {
            buf.writeBoolean(p.visible());
            buf.writeInt(p.ticksFlown());
            buf.writeInt(p.cooldownTicks());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}