package net.aelysium.aelysiummod.sistemas.rp.personagem.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteEscolherPersonagem(String id) implements CustomPacketPayload {

    public static final Type<PacoteEscolherPersonagem> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "personagens_escolher"));

    public static final StreamCodec<FriendlyByteBuf, PacoteEscolherPersonagem> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> buf.writeUtf(pkt.id(), 64),
            buf -> new PacoteEscolherPersonagem(buf.readUtf(64)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
