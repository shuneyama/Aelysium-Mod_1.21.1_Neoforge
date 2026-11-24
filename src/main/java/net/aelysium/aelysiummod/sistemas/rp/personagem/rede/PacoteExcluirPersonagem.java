package net.aelysium.aelysiummod.sistemas.rp.personagem.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteExcluirPersonagem(String id) implements CustomPacketPayload {

    public static final Type<PacoteExcluirPersonagem> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "personagens_excluir"));

    public static final StreamCodec<FriendlyByteBuf, PacoteExcluirPersonagem> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> buf.writeUtf(pkt.id(), 64),
            buf -> new PacoteExcluirPersonagem(buf.readUtf(64)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
