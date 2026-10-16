package net.aelysium.aelysiummod.sistemas.rp.raca.rede;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteVooValkyria(boolean visible, int ticksFlown, int cooldownTicks) implements CustomPacketPayload {

    public static final Type<PacoteVooValkyria> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aelysiummod", "valkyria_flight"));

    public static final StreamCodec<FriendlyByteBuf, PacoteVooValkyria> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PacoteVooValkyria decode(FriendlyByteBuf buf) {
            return new PacoteVooValkyria(buf.readBoolean(), buf.readInt(), buf.readInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, PacoteVooValkyria p) {
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