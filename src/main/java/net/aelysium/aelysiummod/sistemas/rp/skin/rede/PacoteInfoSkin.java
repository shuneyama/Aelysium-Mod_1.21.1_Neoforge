package net.aelysium.aelysiummod.sistemas.rp.skin.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteInfoSkin(String tipo, String id, boolean definida, String modelo, boolean cpm) implements CustomPacketPayload {

    public static final Type<PacoteInfoSkin> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "skin_rp_info"));

    public static final StreamCodec<FriendlyByteBuf, PacoteInfoSkin> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUtf(pkt.tipo(), 16);
                buf.writeUtf(pkt.id(), 64);
                buf.writeBoolean(pkt.definida());
                buf.writeUtf(pkt.modelo(), 8);
                buf.writeBoolean(pkt.cpm());
            },
            buf -> new PacoteInfoSkin(buf.readUtf(16), buf.readUtf(64), buf.readBoolean(), buf.readUtf(8), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
