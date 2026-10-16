package net.aelysium.aelysiummod.conteudo.item.glock.rede;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteSomGlock(boolean tiro, double x, double y, double z) implements CustomPacketPayload {

    public static final Type<PacoteSomGlock> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aelysiummod", "glock_som"));

    public static final StreamCodec<FriendlyByteBuf, PacoteSomGlock> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PacoteSomGlock decode(FriendlyByteBuf buf) {
            return new PacoteSomGlock(buf.readBoolean(), buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        @Override
        public void encode(FriendlyByteBuf buf, PacoteSomGlock p) {
            buf.writeBoolean(p.tiro());
            buf.writeDouble(p.x());
            buf.writeDouble(p.y());
            buf.writeDouble(p.z());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
