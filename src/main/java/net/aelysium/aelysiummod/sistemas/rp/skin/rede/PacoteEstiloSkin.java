package net.aelysium.aelysiummod.sistemas.rp.skin.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteEstiloSkin(String tipo, String id, String modelo) implements CustomPacketPayload {

    public static final Type<PacoteEstiloSkin> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "skin_rp_estilo"));

    public static final StreamCodec<FriendlyByteBuf, PacoteEstiloSkin> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUtf(pkt.tipo(), 16);
                buf.writeUtf(pkt.id(), 64);
                buf.writeUtf(pkt.modelo(), 8);
            },
            buf -> new PacoteEstiloSkin(buf.readUtf(16), buf.readUtf(64), buf.readUtf(8)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
