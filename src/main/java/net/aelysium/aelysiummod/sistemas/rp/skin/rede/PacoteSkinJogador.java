package net.aelysium.aelysiummod.sistemas.rp.skin.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record PacoteSkinJogador(UUID jogador, String modelo, byte[] dados) implements CustomPacketPayload {

    public static final Type<PacoteSkinJogador> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "skin_rp_jogador"));

    public static final StreamCodec<FriendlyByteBuf, PacoteSkinJogador> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUUID(pkt.jogador());
                buf.writeUtf(pkt.modelo(), 8);
                buf.writeByteArray(pkt.dados());
            },
            buf -> new PacoteSkinJogador(buf.readUUID(), buf.readUtf(8), buf.readByteArray(1_000_000)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
