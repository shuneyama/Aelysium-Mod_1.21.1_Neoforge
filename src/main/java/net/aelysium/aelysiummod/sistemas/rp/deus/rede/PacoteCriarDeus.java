package net.aelysium.aelysiummod.sistemas.rp.deus.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteCriarDeus(String nome, int cor, String badge, int corParticula, String pronomes, int poderes) implements CustomPacketPayload {

    public static final Type<PacoteCriarDeus> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "deuses_criar"));

    public static final StreamCodec<FriendlyByteBuf, PacoteCriarDeus> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUtf(pkt.nome(), 64);
                buf.writeInt(pkt.cor());
                buf.writeUtf(pkt.badge(), 64);
                buf.writeInt(pkt.corParticula());
                buf.writeUtf(pkt.pronomes(), 64);
                buf.writeInt(pkt.poderes());
            },
            buf -> new PacoteCriarDeus(buf.readUtf(64), buf.readInt(), buf.readUtf(64), buf.readInt(), buf.readUtf(64), buf.readInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
