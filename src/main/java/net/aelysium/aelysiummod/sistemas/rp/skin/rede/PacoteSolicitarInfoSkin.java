package net.aelysium.aelysiummod.sistemas.rp.skin.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteSolicitarInfoSkin(String tipo, String id) implements CustomPacketPayload {

    public static final Type<PacoteSolicitarInfoSkin> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "skin_rp_solicitar_info"));

    public static final StreamCodec<FriendlyByteBuf, PacoteSolicitarInfoSkin> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUtf(pkt.tipo(), 16);
                buf.writeUtf(pkt.id(), 64);
            },
            buf -> new PacoteSolicitarInfoSkin(buf.readUtf(16), buf.readUtf(64)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
