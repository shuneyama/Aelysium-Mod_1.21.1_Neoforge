package net.aelysium.aelysiummod.sistemas.rp.deus.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteExcluirDeus(String id) implements CustomPacketPayload {

    public static final Type<PacoteExcluirDeus> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "deuses_excluir"));

    public static final StreamCodec<FriendlyByteBuf, PacoteExcluirDeus> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> buf.writeUtf(pkt.id(), 64),
            buf -> new PacoteExcluirDeus(buf.readUtf(64)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
